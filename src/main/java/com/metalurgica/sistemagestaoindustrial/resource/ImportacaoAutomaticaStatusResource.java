package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.response.ImportacaoAutomaticaStatusResponse;
import com.metalurgica.sistemagestaoindustrial.service.ImportacaoAutomaticaStatusService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/importacoes/automaticas/status")
public class ImportacaoAutomaticaStatusResource {
    private final ImportacaoAutomaticaStatusService service;
    public ImportacaoAutomaticaStatusResource(ImportacaoAutomaticaStatusService service) { this.service = service; }
    @GetMapping
    @PreAuthorize("hasAuthority('OPERACAO_MONITORAR')")
    public ImportacaoAutomaticaStatusResponse status(@PathVariable Long empresaId) { return service.status(empresaId); }
}
