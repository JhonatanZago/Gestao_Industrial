package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.request.FuncionarioRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.FuncionarioResponse;
import com.metalurgica.sistemagestaoindustrial.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/funcionarios")
public class FuncionarioResource {
    private final FuncionarioService service;
    public FuncionarioResource(FuncionarioService service) { this.service = service; }

    @PostMapping
    public org.springframework.http.ResponseEntity<FuncionarioResponse> create(@PathVariable Long empresaId, @Valid @RequestBody FuncionarioRequest request) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(empresaId, request));
    }

    @GetMapping
    public Page<FuncionarioResponse> list(@PathVariable Long empresaId, @RequestParam(required = false) Boolean ativo,
                                          @RequestParam(required = false) String codigo, @RequestParam(required = false) String nome,
                                          @PageableDefault(size = 20) Pageable pageable) {
        return service.list(empresaId, ativo, codigo, nome, pageable);
    }

    @GetMapping("/{funcionarioId}")
    public FuncionarioResponse get(@PathVariable Long empresaId, @PathVariable Long funcionarioId) { return service.get(empresaId, funcionarioId); }

    @PutMapping("/{funcionarioId}")
    public FuncionarioResponse update(@PathVariable Long empresaId, @PathVariable Long funcionarioId, @Valid @RequestBody FuncionarioRequest request) {
        return service.update(empresaId, funcionarioId, request);
    }

    @PatchMapping("/{funcionarioId}/ativar")
    public FuncionarioResponse activate(@PathVariable Long empresaId, @PathVariable Long funcionarioId) { return service.setActive(empresaId, funcionarioId, true); }

    @PatchMapping("/{funcionarioId}/desativar")
    public FuncionarioResponse deactivate(@PathVariable Long empresaId, @PathVariable Long funcionarioId) { return service.setActive(empresaId, funcionarioId, false); }
}
