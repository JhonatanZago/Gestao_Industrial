package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:auditoria_rollback;MODE=PostgreSQL;DB_CLOSE_DELAY=0;DATABASE_TO_LOWER=TRUE")
class AuditoriaRollbackIntegrationTests {
    @org.springframework.beans.factory.annotation.Autowired com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository empresaRepository;
    @org.springframework.beans.factory.annotation.Autowired EventoAuditoriaRepository eventoRepository;
    @org.springframework.beans.factory.annotation.Autowired AuditoriaService auditoriaService;
    @org.springframework.beans.factory.annotation.Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Test
    void falhaDaOperacaoFazRollbackDoEventoDeAuditoria() {
        var empresa = new Empresa();
        empresa.setRazaoSocial("Empresa Rollback Auditoria");
        empresa.setNomeFantasia("Empresa Rollback Auditoria");
        empresa.setCnpj(String.format("%014d", Math.abs(System.nanoTime() % 100000000000000L)));
        empresa.touchForCreate();
        empresa = empresaRepository.save(empresa);
        var before = eventoRepository.count();

        final Long empresaId = empresa.getId();
        var transactions = new TransactionTemplate(transactionManager);
        assertThrows(IllegalStateException.class, () -> transactions.executeWithoutResult(status -> {
            auditoriaService.record(empresaId, TipoRecursoAuditoria.IMPORTACAO, 999L,
                    AcaoAuditoria.IMPORTACAO, OrigemAuditoria.SISTEMA,
                    null, Map.of("status", "CONCLUIDO"), Map.of("setorId", 1L), "rollback-test");
            throw new IllegalStateException("falha simulada antes do commit");
        }));

        assertEquals(before, eventoRepository.count());
    }

}
