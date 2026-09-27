package com.metalurgica.sistemagestaoindustrial.resource;
import com.metalurgica.sistemagestaoindustrial.dto.request.MaquinaRequest; import com.metalurgica.sistemagestaoindustrial.dto.response.MaquinaResponse; import com.metalurgica.sistemagestaoindustrial.service.MaquinaService; import jakarta.validation.Valid; import org.springframework.data.domain.*; import org.springframework.data.web.PageableDefault; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/empresas/{empresaId}/maquinas") public class MaquinaResource {
    private final MaquinaService service; public MaquinaResource(MaquinaService service) { this.service = service; }
    @PostMapping public ResponseEntity<MaquinaResponse> create(@PathVariable Long empresaId, @Valid @RequestBody MaquinaRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, r)); }
    @GetMapping public Page<MaquinaResponse> list(@PathVariable Long empresaId, @RequestParam(required=false) Long setorId, @RequestParam(required=false) String codigo, @RequestParam(required=false) String nome, @RequestParam(required=false) String tipo, @RequestParam(required=false) Boolean ativo, @PageableDefault(size=20) Pageable p) { return service.list(empresaId, setorId, codigo, nome, tipo, ativo, p); }
    @GetMapping("/{id}") public MaquinaResponse get(@PathVariable Long empresaId, @PathVariable Long id) { return service.get(empresaId, id); }
    @PutMapping("/{id}") public MaquinaResponse update(@PathVariable Long empresaId, @PathVariable Long id, @Valid @RequestBody MaquinaRequest r) { return service.update(empresaId, id, r); }
    @PatchMapping("/{id}/ativar") public MaquinaResponse activate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, true); }
    @PatchMapping("/{id}/desativar") public MaquinaResponse deactivate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, false); }
}
