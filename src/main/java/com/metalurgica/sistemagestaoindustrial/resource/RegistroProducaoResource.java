package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.request.RegistroProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.RegistroProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.service.RegistroProducaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/producoes")
public class RegistroProducaoResource {
    private final RegistroProducaoService service;
    public RegistroProducaoResource(RegistroProducaoService service) { this.service = service; }

    @PostMapping
    public org.springframework.http.ResponseEntity<RegistroProducaoResponse> create(@PathVariable Long empresaId, @Valid @RequestBody RegistroProducaoRequest request) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, request));
    }

    @GetMapping
    public Page<RegistroProducaoResponse> list(@PathVariable Long empresaId,
                                               @RequestParam(required = false) Long setorId,
                                               @RequestParam(required = false) Long funcionarioId,
                                               @RequestParam(required = false) Long pecaId,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
                                               @RequestParam(required = false) Boolean ativo,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return service.list(empresaId, setorId, funcionarioId, pecaId, inicio, fim, ativo, pageable);
    }

    @GetMapping("/{producaoId}")
    public RegistroProducaoResponse get(@PathVariable Long empresaId, @PathVariable Long producaoId) { return service.get(empresaId, producaoId); }

    @PutMapping("/{producaoId}")
    public RegistroProducaoResponse update(@PathVariable Long empresaId, @PathVariable Long producaoId, @Valid @RequestBody RegistroProducaoRequest request) {
        return service.update(empresaId, producaoId, request);
    }

    @PatchMapping("/{producaoId}/ativar")
    public RegistroProducaoResponse activate(@PathVariable Long empresaId, @PathVariable Long producaoId) { return service.setActive(empresaId, producaoId, true); }

    @PatchMapping("/{producaoId}/desativar")
    public RegistroProducaoResponse deactivate(@PathVariable Long empresaId, @PathVariable Long producaoId) { return service.setActive(empresaId, producaoId, false); }
}
