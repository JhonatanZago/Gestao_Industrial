package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.request.PecaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.PecaResponse;
import com.metalurgica.sistemagestaoindustrial.service.PecaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/pecas")
public class PecaResource {
    private final PecaService service;
    public PecaResource(PecaService service) { this.service = service; }

    @PostMapping
    public org.springframework.http.ResponseEntity<PecaResponse> create(@PathVariable Long empresaId, @Valid @RequestBody PecaRequest request) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, request));
    }

    @GetMapping
    public Page<PecaResponse> list(@PathVariable Long empresaId, @RequestParam(required = false) Boolean ativo,
                                   @RequestParam(required = false) String codigo, @RequestParam(required = false) String descricao,
                                   @PageableDefault(size = 20) Pageable pageable) {
        return service.list(empresaId, ativo, codigo, descricao, pageable);
    }

    @GetMapping("/{pecaId}")
    public PecaResponse get(@PathVariable Long empresaId, @PathVariable Long pecaId) { return service.get(empresaId, pecaId); }

    @PutMapping("/{pecaId}")
    public PecaResponse update(@PathVariable Long empresaId, @PathVariable Long pecaId, @Valid @RequestBody PecaRequest request) {
        return service.update(empresaId, pecaId, request);
    }

    @PatchMapping("/{pecaId}/ativar")
    public PecaResponse activate(@PathVariable Long empresaId, @PathVariable Long pecaId) { return service.setActive(empresaId, pecaId, true); }

    @PatchMapping("/{pecaId}/desativar")
    public PecaResponse deactivate(@PathVariable Long empresaId, @PathVariable Long pecaId) { return service.setActive(empresaId, pecaId, false); }
}
