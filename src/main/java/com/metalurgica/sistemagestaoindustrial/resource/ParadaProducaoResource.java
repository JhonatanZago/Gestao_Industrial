package com.metalurgica.sistemagestaoindustrial.resource;
import com.metalurgica.sistemagestaoindustrial.dto.request.ParadaProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.ParadaProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.service.ParadaProducaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas")
public class ParadaProducaoResource {
    private final ParadaProducaoService service; public ParadaProducaoResource(ParadaProducaoService service) { this.service = service; }
    @PostMapping public org.springframework.http.ResponseEntity<ParadaProducaoResponse> create(@PathVariable Long empresaId, @PathVariable Long producaoId, @Valid @RequestBody ParadaProducaoRequest req) { return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, producaoId, req)); }
    @GetMapping public Page<ParadaProducaoResponse> list(@PathVariable Long empresaId, @PathVariable Long producaoId, @PageableDefault(size = 20) Pageable pageable) { return service.list(empresaId, producaoId, pageable); }
    @GetMapping("/{paradaId}") public ParadaProducaoResponse get(@PathVariable Long empresaId, @PathVariable Long producaoId, @PathVariable Long paradaId) { return service.get(empresaId, producaoId, paradaId); }
    @PutMapping("/{paradaId}") public ParadaProducaoResponse update(@PathVariable Long empresaId, @PathVariable Long producaoId, @PathVariable Long paradaId, @Valid @RequestBody ParadaProducaoRequest req) { return service.update(empresaId, producaoId, paradaId, req); }
    @PatchMapping("/{paradaId}/ativar") public ParadaProducaoResponse activate(@PathVariable Long empresaId, @PathVariable Long producaoId, @PathVariable Long paradaId) { return service.setActive(empresaId, producaoId, paradaId, true); }
    @PatchMapping("/{paradaId}/desativar") public ParadaProducaoResponse deactivate(@PathVariable Long empresaId, @PathVariable Long producaoId, @PathVariable Long paradaId) { return service.setActive(empresaId, producaoId, paradaId, false); }
}
