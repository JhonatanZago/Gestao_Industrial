package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.domain.TipoMeta;
import com.metalurgica.sistemagestaoindustrial.dto.request.MetaProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.MetaProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.service.MetaProducaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/empresas/{empresaId}/metas-producao")
public class MetaProducaoResource {
    private final MetaProducaoService service; public MetaProducaoResource(MetaProducaoService service) { this.service = service; }
    @PostMapping public ResponseEntity<MetaProducaoResponse> create(@PathVariable Long empresaId, @Valid @RequestBody MetaProducaoRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, r)); }
    @GetMapping public Page<MetaProducaoResponse> list(@PathVariable Long empresaId, @RequestParam(required=false) TipoMeta tipo, @RequestParam(required=false) Boolean ativo, @PageableDefault(size=20) Pageable p) { return service.list(empresaId, tipo, ativo, p); }
    @GetMapping("/{id}") public MetaProducaoResponse get(@PathVariable Long empresaId, @PathVariable Long id) { return service.get(empresaId, id); }
    @PutMapping("/{id}") public MetaProducaoResponse update(@PathVariable Long empresaId, @PathVariable Long id, @Valid @RequestBody MetaProducaoRequest r) { return service.update(empresaId, id, r); }
    @PatchMapping("/{id}/ativar") public MetaProducaoResponse activate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, true); }
    @PatchMapping("/{id}/desativar") public MetaProducaoResponse deactivate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, false); }
}
