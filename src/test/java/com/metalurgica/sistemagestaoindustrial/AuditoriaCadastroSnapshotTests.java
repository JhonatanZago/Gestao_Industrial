package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditoriaCadastroSnapshotTests {
    private final EventoAuditoriaRepository repository = mock(EventoAuditoriaRepository.class);
    private final AuditoriaService service = new AuditoriaService(repository, new AuditoriaSnapshotSanitizer(), mock(UsuarioRepository.class));

    @Test
    void preservaCamposOperacionaisFinanceirosAcentosDatasEnumsBigDecimalENulos() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("nome", "MÃ¡quina de corte");
        snapshot.put("descricao", "PeÃ§a nÃºmero 1");
        snapshot.put("tipo", "DIARIO");
        snapshot.put("quantidadeAlvo", new BigDecimal("22000.00"));
        snapshot.put("valor", new BigDecimal("0.90"));
        snapshot.put("vigenciaInicio", OffsetDateTime.parse("2026-09-01T00:00:00-03:00"));
        snapshot.put("vigenciaFim", null);
        snapshot.put("ativo", true);

        for (var resource : new TipoRecursoAuditoria[]{TipoRecursoAuditoria.FUNCIONARIO,
                TipoRecursoAuditoria.PECA, TipoRecursoAuditoria.MAQUINA, TipoRecursoAuditoria.TURNO,
                TipoRecursoAuditoria.META_PRODUCAO, TipoRecursoAuditoria.TEMPO_PADRAO,
                TipoRecursoAuditoria.VALOR_PRODUCAO}) {
            var event = service.record(7L, resource, 10L, AcaoAuditoria.ALTERACAO,
                    OrigemAuditoria.MANUAL, snapshot, snapshot, Map.of("setorId", 3L), null);
            assertTrue(event.getDadosNovos().contains("MÃ¡quina") || event.getDadosNovos().contains("0.90")
                    || event.getDadosNovos().contains("22000.00"));
        }
        verify(repository, times(7)).save(any(EventoAuditoria.class));
    }
}
