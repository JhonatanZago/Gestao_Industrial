package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import com.metalurgica.sistemagestaoindustrial.dto.request.ValorProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.ValorProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.service.ValorProducaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController @RequestMapping("/api/v1/empresas/{empresaId}/valores-producao")
public class ValorProducaoResource {
    private final ValorProducaoService service; public ValorProducaoResource(ValorProducaoService service) { this.service = service; }
    @PostMapping public ResponseEntity<ValorProducaoResponse> create(@PathVariable Long empresaId, @Valid @RequestBody ValorProducaoRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, r)); }
    @GetMapping public Page<ValorProducaoResponse> list(@PathVariable Long empresaId, @RequestParam(required=false) Long pecaId, @RequestParam(required=false) Long setorId, @RequestParam(required=false) Long maquinaId, @RequestParam(required=false) TipoValor tipoValor, @RequestParam(required=false) String moeda, @RequestParam(required=false) Boolean ativo, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate vigenteEm, @PageableDefault(size=20) Pageable p) { return service.list(empresaId, pecaId, setorId, maquinaId, tipoValor, moeda, ativo, vigenteEm, p); }
    @GetMapping("/{id}") public ValorProducaoResponse get(@PathVariable Long empresaId, @PathVariable Long id) { return service.get(empresaId, id); }
    @PutMapping("/{id}") public ValorProducaoResponse update(@PathVariable Long empresaId, @PathVariable Long id, @Valid @RequestBody ValorProducaoRequest r) { return service.update(empresaId, id, r); }
    @PatchMapping("/{id}/ativar") public ValorProducaoResponse activate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, true); }
    @PatchMapping("/{id}/desativar") public ValorProducaoResponse deactivate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, false); }
}
