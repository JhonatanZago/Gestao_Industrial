package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.assistente.dto.PerguntaAssistenteRequest;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.RespostaAssistente;
import com.metalurgica.sistemagestaoindustrial.assistente.service.AssistenteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/assistente")
public class AssistenteResource {
    private final AssistenteService service;

    public AssistenteResource(AssistenteService service) {
        this.service = service;
    }

    @PostMapping("/perguntar")
    public RespostaAssistente ask(@PathVariable Long empresaId,
                                  @Valid @RequestBody PerguntaAssistenteRequest request,
                                  Authentication authentication) {
        return service.ask(empresaId, request, authentication);
    }
}
