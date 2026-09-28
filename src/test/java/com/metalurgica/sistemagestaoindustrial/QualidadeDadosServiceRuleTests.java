package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.quality.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import com.metalurgica.sistemagestaoindustrial.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QualidadeDadosServiceRuleTests {
    @Test
    void diagnosticaFimAnteriorMesmoQuandoORegistroLegadoNaoPassariaPelaApiAtual() {
        var productionRepository = mock(RegistroProducaoRepository.class);
        var stopRepository = mock(ParadaProducaoRepository.class);
        var tempoRepository = mock(TempoPadraoProducaoRepository.class);
        var valueRepository = mock(ValorProducaoRepository.class);
        var goalRepository = mock(MetaProducaoRepository.class);
        var validator = mock(AnalyticsQueryValidator.class);
        var scope = mock(SectorScopeService.class);
        var production = production();
        production.setInicio(LocalDateTime.of(2026, 9, 26, 10, 0));
        production.setFim(LocalDateTime.of(2026, 9, 26, 9, 0));
        when(productionRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(production));
        when(stopRepository.findByRegistroProducaoIdInAndAtivoTrue(any())).thenReturn(List.of());
        when(tempoRepository.findActiveForPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(valueRepository.findActiveForPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(goalRepository.findActiveForPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(scope.currentScope(anyLong())).thenReturn(Optional.empty());

        var service = new QualidadeDadosService(productionRepository, stopRepository, tempoRepository, valueRepository, goalRepository,
                mock(ImportacaoProducaoRepository.class), mock(SetorRepository.class), mock(FuncionarioRepository.class), mock(PecaRepository.class),
                mock(MaquinaRepository.class), mock(TurnoRepository.class), mock(EmpresaService.class), validator, scope,
                new AplicabilidadeHistoricaService(), new QualidadeDadosProperties());
        var query = new QualidadeDadosQuery(LocalDateTime.of(2026, 9, 26, 0, 0), LocalDateTime.of(2026, 9, 26, 23, 59), null, null, null, null, null, null, null, null, null, null);

        var result = service.problemas(1L, query, PageRequest.of(0, 50));

        assertEquals(1, result.getContent().stream().filter(p -> "QD-TEMP-001".equals(p.codigo())).count());
    }

    private RegistroProducao production() {
        var company = new Empresa(); company.setId(1L);
        var sector = new Setor(); sector.setId(2L);
        var employee = new Funcionario(); employee.setId(3L);
        var part = new Peca(); part.setId(4L);
        var value = new RegistroProducao(); value.setId(5L); value.setEmpresa(company); value.setSetor(sector); value.setFuncionario(employee); value.setPeca(part); value.setQuantidade(BigDecimal.ONE);
        return value;
    }
}
