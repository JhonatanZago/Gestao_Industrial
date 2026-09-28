package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.audit.AuditoriaConsultaService;
import com.metalurgica.sistemagestaoindustrial.audit.TipoRecursoAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.AcaoAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.OrigemAuditoria;
import com.metalurgica.sistemagestaoindustrial.dto.request.AuditoriaFiltro;
import com.metalurgica.sistemagestaoindustrial.dto.response.EventoAuditoriaDetalheResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.EventoAuditoriaResumoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/auditoria")
public class AuditoriaResource {
    private final AuditoriaConsultaService service;

    public AuditoriaResource(AuditoriaConsultaService service) { this.service = service; }

    @GetMapping
    public Page<EventoAuditoriaResumoResponse> list(@PathVariable Long empresaId,
                                                    @RequestParam(required = false) OffsetDateTime inicio,
                                                    @RequestParam(required = false) OffsetDateTime fim,
                                                    @RequestParam(required = false) Long usuarioId,
                                                    @RequestParam(required = false) TipoRecursoAuditoria tipoRecurso,
                                                    @RequestParam(required = false) Long recursoId,
                                                    @RequestParam(required = false) AcaoAuditoria acao,
                                                    @RequestParam(required = false) OrigemAuditoria origem,
                                                    @RequestParam(required = false) String correlationId,
                                                    @PageableDefault(size = 20) Pageable pageable,
                                                    Authentication authentication) {
        return service.list(empresaId, new AuditoriaFiltro(inicio, fim, usuarioId, tipoRecurso, recursoId, acao, origem, correlationId), pageable, authentication);
    }

    @GetMapping("/{tipoRecurso}/{recursoId}")
    public Page<EventoAuditoriaDetalheResponse> history(@PathVariable Long empresaId,
                                                         @PathVariable TipoRecursoAuditoria tipoRecurso,
                                                         @PathVariable Long recursoId,
                                                         @PageableDefault(size = 20) Pageable pageable,
                                                         Authentication authentication) {
        return service.resourceHistory(empresaId, tipoRecurso, recursoId, pageable, authentication);
    }

    @GetMapping("eventos/{eventoId}")
    public EventoAuditoriaDetalheResponse event(@PathVariable Long empresaId, @PathVariable Long eventoId,
                                                Authentication authentication) {
        return service.event(empresaId, eventoId, authentication);
    }
}
