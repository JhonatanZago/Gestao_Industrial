package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.request.SetorRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.SetorResponse;
import com.metalurgica.sistemagestaoindustrial.service.SetorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/setores")
public class SetorResource {
    private final SetorService service;
    public SetorResource(SetorService service) { this.service = service; }

    @PostMapping public org.springframework.http.ResponseEntity<SetorResponse> create(@PathVariable Long empresaId, @Valid @RequestBody SetorRequest request) { return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, request)); }
    @GetMapping public Page<SetorResponse> list(@PathVariable Long empresaId, @RequestParam(required = false) Boolean ativo, @RequestParam(required = false) String nome, @RequestParam(required = false) String codigo, @PageableDefault(size = 20) Pageable pageable) { return service.list(empresaId, ativo, nome, codigo, pageable); }
    @GetMapping("/{setorId}") public SetorResponse get(@PathVariable Long empresaId, @PathVariable Long setorId) { return service.get(empresaId, setorId); }
    @PutMapping("/{setorId}") public SetorResponse update(@PathVariable Long empresaId, @PathVariable Long setorId, @Valid @RequestBody SetorRequest request) { return service.update(empresaId, setorId, request); }
    @PatchMapping("/{setorId}/ativar") public SetorResponse activate(@PathVariable Long empresaId, @PathVariable Long setorId) { return service.setActive(empresaId, setorId, true); }
    @PatchMapping("/{setorId}/desativar") public SetorResponse deactivate(@PathVariable Long empresaId, @PathVariable Long setorId) { return service.setActive(empresaId, setorId, false); }
}
