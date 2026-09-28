package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.quality.AplicabilidadeHistoricaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AplicabilidadeHistoricaServiceTests {
    private final AplicabilidadeHistoricaService service = new AplicabilidadeHistoricaService();

    @Test
    void naoAplicaConfiguracaoQueAindaNaoEraValidaNaDataDaProducao() {
        var empresa = new Empresa(); empresa.setId(1L);
        var setor = new Setor(); setor.setId(2L);
        var peca = new Peca(); peca.setId(3L);
        var production = new RegistroProducao(); production.setEmpresa(empresa); production.setSetor(setor); production.setPeca(peca);
        production.setInicio(LocalDateTime.of(2026, 1, 10, 8, 0));
        var future = standard(empresa, setor, peca, LocalDate.of(2026, 2, 1));

        assertNull(service.tempoPadrao(List.of(future), production));
        future.setVigenciaInicio(LocalDate.of(2026, 1, 1));
        assertSame(future, service.tempoPadrao(List.of(future), production));
    }

    @Test
    void priorizaConfiguracaoEspecificaDeMaquina() {
        var empresa = new Empresa(); empresa.setId(1L);
        var setor = new Setor(); setor.setId(2L);
        var peca = new Peca(); peca.setId(3L);
        var maquina = new Maquina(); maquina.setId(4L);
        var production = new RegistroProducao(); production.setEmpresa(empresa); production.setSetor(setor); production.setPeca(peca); production.setMaquina(maquina);
        production.setInicio(LocalDateTime.of(2026, 1, 10, 8, 0));
        var global = standard(empresa, setor, peca, LocalDate.of(2026, 1, 1));
        var specific = standard(empresa, setor, peca, LocalDate.of(2026, 1, 1)); specific.setMaquina(maquina); specific.setSegundos(new BigDecimal("12"));

        assertSame(specific, service.tempoPadrao(List.of(global, specific), production));
    }

    private TempoPadraoProducao standard(Empresa empresa, Setor setor, Peca peca, LocalDate start) {
        var value = new TempoPadraoProducao();
        value.setEmpresa(empresa); value.setSetor(setor); value.setPeca(peca); value.setSegundos(new BigDecimal("30"));
        value.setBaseTempo(BaseTempoPadrao.POR_UNIDADE); value.setVigenciaInicio(start); value.setAtivo(true);
        return value;
    }
}
