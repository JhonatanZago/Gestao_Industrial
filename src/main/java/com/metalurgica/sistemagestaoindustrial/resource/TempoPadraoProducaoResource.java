package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.request.TempoPadraoProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.TempoPadraoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.service.TempoPadraoProducaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController @RequestMapping("/api/v1/empresas/{empresaId}/tempos-padrao")
public class TempoPadraoProducaoResource {
    private final TempoPadraoProducaoService service;
    public TempoPadraoProducaoResource(TempoPadraoProducaoService service) { this.service = service; }
    @PostMapping public ResponseEntity<TempoPadraoProducaoResponse> create(@PathVariable Long empresaId, @Valid @RequestBody TempoPadraoProducaoRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, r)); }
    @GetMapping public Page<TempoPadraoProducaoResponse> list(@PathVariable Long empresaId, @RequestParam(required=false) Long pecaId, @RequestParam(required=false) Long setorId, @RequestParam(required=false) Long maquinaId, @RequestParam(required=false) Boolean ativo, @RequestParam(required=false) LocalDate vigenteEm, @PageableDefault(size=20) Pageable p) { return service.list(empresaId, pecaId, setorId, maquinaId, ativo, vigenteEm, p); }
    @GetMapping("/{id}") public TempoPadraoProducaoResponse get(@PathVariable Long empresaId, @PathVariable Long id) { return service.get(empresaId, id); }
    @PutMapping("/{id}") public TempoPadraoProducaoResponse update(@PathVariable Long empresaId, @PathVariable Long id, @Valid @RequestBody TempoPadraoProducaoRequest r) { return service.update(empresaId, id, r); }
    @PatchMapping("/{id}/ativar") public TempoPadraoProducaoResponse activate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, true); }
    @PatchMapping("/{id}/desativar") public TempoPadraoProducaoResponse deactivate(@PathVariable Long empresaId, @PathVariable Long id) { return service.setActive(empresaId, id, false); }
}
