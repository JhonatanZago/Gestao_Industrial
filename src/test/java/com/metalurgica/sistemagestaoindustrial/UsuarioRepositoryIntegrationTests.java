package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.domain.Setor;
import com.metalurgica.sistemagestaoindustrial.domain.StatusUsuario;
import com.metalurgica.sistemagestaoindustrial.domain.UsuarioSetor;
import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.repository.SetorRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioSetorRepository;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UsuarioRepositoryIntegrationTests {
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SetorRepository setorRepository;
    @Autowired PerfilRepository perfilRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired UsuarioSetorRepository usuarioSetorRepository;
    @Autowired UsuarioService usuarioService;

    @Test
    void isolatesEmailCompanyStatusProfileAndSectorAssociations() {
        var companyA = company("A");
        var companyB = company("B");
        var sectorA = sector(companyA, "A1");
        var sectorB = sector(companyB, "B1");
        var admin = perfilRepository.findByCodigo("ADMINISTRADOR").orElseThrow();
        var operational = perfilRepository.findByCodigo("OPERACIONAL").orElseThrow();

        var first = usuarioService.create(companyA, admin, "Admin A", "same@example.test", "SenhaSegura123");
        var second = usuarioService.create(companyB, operational, "Operacional B", "other@example.test", "SenhaSegura123");
        assertThrows(IllegalArgumentException.class,
            () -> usuarioService.create(companyB, operational, "Duplicado", "same@example.test", "SenhaSegura123"));

        first.setStatus(StatusUsuario.BLOQUEADO);
        usuarioRepository.save(first);
        assertEquals(1, usuarioRepository.findByEmpresaIdAndStatus(companyA.getId(), StatusUsuario.BLOQUEADO, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, usuarioRepository.findByEmpresaId(companyB.getId(), PageRequest.of(0, 10)).getTotalElements());
        assertEquals("ADMINISTRADOR", first.getPerfil().getCodigo());

        var association = new UsuarioSetor();
        association.setUsuario(second);
        association.setSetor(sectorB);
        usuarioSetorRepository.save(association);
        assertTrue(usuarioSetorRepository.existsByUsuarioIdAndSetorId(second.getId(), sectorB.getId()));
        assertEquals(0, usuarioSetorRepository.findByUsuarioId(first.getId()).size());
        assertEquals(companyA.getId(), sectorA.getEmpresa().getId());
    }

    private Empresa company(String suffix) {
        var company = new Empresa();
        company.setRazaoSocial("Empresa " + suffix);
        company.setNomeFantasia("Empresa " + suffix);
        company.setCnpj(String.format("%014d", Math.abs(System.nanoTime() % 100000000000000L)));
        company.touchForCreate();
        return empresaRepository.save(company);
    }

    private Setor sector(Empresa company, String code) {
        var sector = new Setor();
        sector.setEmpresa(company);
        sector.setCodigo(code);
        sector.setNome("Setor " + code);
        sector.setNomeNormalizado(code.toLowerCase());
        sector.touchForCreate();
        return setorRepository.save(sector);
    }
}
