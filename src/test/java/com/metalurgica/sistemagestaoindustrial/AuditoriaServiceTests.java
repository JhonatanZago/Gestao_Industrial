package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditoriaServiceTests {
    private final EventoAuditoriaRepository repository = mock(EventoAuditoriaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AuditoriaService service = new AuditoriaService(repository, new AuditoriaSnapshotSanitizer(), usuarioRepository);

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void operationWithoutHumanUsesSystemOriginAndNoUser() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var event = service.record(7L, TipoRecursoAuditoria.REGISTRO_PRODUCAO, 100L,
                AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL,
                Map.of("quantidade", 500), Map.of("quantidade", 550), null, null);

        assertNull(event.getUsuarioId());
        assertEquals(OrigemAuditoria.SISTEMA, event.getOrigem());
        assertTrue(event.getDadosAnteriores().contains("500"));
        verify(repository).save(any(EventoAuditoria.class));
    }

    @Test
    void passwordChangeHasNoSnapshots() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var event = service.recordPasswordChange(7L, 9L, OrigemAuditoria.API);

        assertEquals(AcaoAuditoria.ALTERACAO_SENHA, event.getAcao());
        assertNull(event.getDadosAnteriores());
        assertNull(event.getDadosNovos());
    }

    @Test
    void processaLoteSinteticoDeEventosDentroDoLimiteDeSnapshot() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        for (long id = 1; id <= 1000; id++) {
            service.record(7L, TipoRecursoAuditoria.REGISTRO_PRODUCAO, id,
                    AcaoAuditoria.CRIACAO, OrigemAuditoria.IMPORTACAO_EXCEL,
                    null, Map.of("quantidade", id, "observacao", "linha" + id),
                    Map.of("setorId", 3L), "volume-test");
        }
        verify(repository, times(1000)).save(any(EventoAuditoria.class));
    }
}
