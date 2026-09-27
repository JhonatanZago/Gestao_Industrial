package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.request.MotivoParadaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.MotivoParadaResponse;
import com.metalurgica.sistemagestaoindustrial.service.MotivoParadaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/empresas/{empresaId}/motivos-parada")
public class MotivoParadaResource {
    private final MotivoParadaService service; public MotivoParadaResource(MotivoParadaService service) { this.service = service; }
    @PostMapping public org.springframework.http.ResponseEntity<MotivoParadaResponse> create(@PathVariable Long empresaId, @Valid @RequestBody MotivoParadaRequest req) { return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, req)); }
    @GetMapping public Page<MotivoParadaResponse> list(@PathVariable Long empresaId, @RequestParam(required = false) Boolean ativo, @RequestParam(required = false) String codigo, @RequestParam(required = false) String nome, @RequestParam(required = false) String categoria, @PageableDefault(size = 20) Pageable pageable) { return service.list(empresaId, ativo, codigo, nome, categoria, pageable); }
    @GetMapping("/{motivoId}") public MotivoParadaResponse get(@PathVariable Long empresaId, @PathVariable Long motivoId) { return service.get(empresaId, motivoId); }
    @PutMapping("/{motivoId}") public MotivoParadaResponse update(@PathVariable Long empresaId, @PathVariable Long motivoId, @Valid @RequestBody MotivoParadaRequest req) { return service.update(empresaId, motivoId, req); }
    @PatchMapping("/{motivoId}/ativar") public MotivoParadaResponse activate(@PathVariable Long empresaId, @PathVariable Long motivoId) { return service.setActive(empresaId, motivoId, true); }
    @PatchMapping("/{motivoId}/desativar") public MotivoParadaResponse deactivate(@PathVariable Long empresaId, @PathVariable Long motivoId) { return service.setActive(empresaId, motivoId, false); }
}
