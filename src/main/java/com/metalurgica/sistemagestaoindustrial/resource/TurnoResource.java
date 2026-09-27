package com.metalurgica.sistemagestaoindustrial.resource;
import com.metalurgica.sistemagestaoindustrial.dto.request.TurnoRequest; import com.metalurgica.sistemagestaoindustrial.dto.response.TurnoResponse; import com.metalurgica.sistemagestaoindustrial.service.TurnoService; import jakarta.validation.Valid; import org.springframework.data.domain.*; import org.springframework.data.web.PageableDefault; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/empresas/{empresaId}/turnos") public class TurnoResource {
    private final TurnoService service; public TurnoResource(TurnoService service) { this.service = service; }
    @PostMapping public ResponseEntity<TurnoResponse> create(@PathVariable Long empresaId, @Valid @RequestBody TurnoRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, r)); }
    @GetMapping public Page<TurnoResponse> list(@PathVariable Long empresaId, @RequestParam(required=false) Boolean ativo, @RequestParam(required=false) String codigo, @RequestParam(required=false) String nome, @PageableDefault(size=20) Pageable p) { return service.list(empresaId, ativo, codigo, nome, p); }
    @GetMapping("/{id}") public TurnoResponse get(@PathVariable Long empresaId, @PathVariable Long id) { return service.get(empresaId, id); }
    @PutMapping("/{id}") public TurnoResponse update(@PathVariable Long empresaId, @PathVariable Long id, @Valid @RequestBody TurnoRequest r) { return service.update(empresaId, id, r); }
    @PatchMapping("/{id}/ativar") public TurnoResponse activate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, true); }
    @PatchMapping("/{id}/desativar") public TurnoResponse deactivate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, false); }
}
