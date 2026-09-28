package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.assistente.ai.*;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContextResolver;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/assistente/status")
public class AssistenteStatusResource {
    private final AiProperties properties;
    private final AssistenteSecurityContextResolver contextResolver;
    private final ChatModelGateway chatGateway;
    private final EmbeddingGateway embeddingGateway;
    private final VectorStorePort vectorStore;

    public AssistenteStatusResource(AiProperties properties, AssistenteSecurityContextResolver contextResolver,
                                    ChatModelGateway chatGateway, EmbeddingGateway embeddingGateway,
                                    VectorStorePort vectorStore) {
        this.properties = properties;
        this.contextResolver = contextResolver;
        this.chatGateway = chatGateway;
        this.embeddingGateway = embeddingGateway;
        this.vectorStore = vectorStore;
    }

    @GetMapping
    public StatusResponse status(@PathVariable Long empresaId, Authentication authentication) {
        var context = contextResolver.resolve(authentication, empresaId);
        if (!context.hasPermission("AI_DOCUMENTO_GERENCIAR")) {
            throw new org.springframework.security.access.AccessDeniedException("Permissão administrativa insuficiente.");
        }
        return new StatusResponse(properties.isEnabled(), availability(chatGateway), availability(embeddingGateway),
                availability(vectorStore), properties.getMaxToolCalls(), properties.getMaxOutputTokens());
    }

    private String availability(Object gateway) {
        if (gateway instanceof DisabledChatModelGateway || gateway instanceof DisabledEmbeddingGateway
                || gateway instanceof DisabledVectorStore) return "INDISPONIVEL";
        return "CONFIGURADO";
    }

    public record StatusResponse(boolean enabled, String chat, String embeddings, String vectorStore,
                                 int maxToolCalls, int maxOutputTokens) { }
}
