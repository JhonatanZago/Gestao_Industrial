package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.request.EmpresaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.EmpresaResponse;
import com.metalurgica.sistemagestaoindustrial.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaResource {
    private final EmpresaService service;
    public EmpresaResource(EmpresaService service) { this.service = service; }

    @PostMapping public org.springframework.http.ResponseEntity<EmpresaResponse> create(@Valid @RequestBody EmpresaRequest request) { return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @GetMapping public Page<EmpresaResponse> list(@RequestParam(required = false) Boolean ativo, @PageableDefault(size = 20) Pageable pageable) { return service.list(ativo, pageable); }
    @GetMapping("/{id}") public EmpresaResponse get(@PathVariable Long id) { return service.get(id); }
    @PutMapping("/{id}") public EmpresaResponse update(@PathVariable Long id, @Valid @RequestBody EmpresaRequest request) { return service.update(id, request); }
    @PatchMapping("/{id}/ativar") public EmpresaResponse activate(@PathVariable Long id) { return service.setActive(id, true); }
    @PatchMapping("/{id}/desativar") public EmpresaResponse deactivate(@PathVariable Long id) { return service.setActive(id, false); }
}
