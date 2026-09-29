package com.metalurgica.sistemagestaoindustrial.observability;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.*;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import org.springframework.stereotype.Service;
@Service
public class AiAvailabilityService {
    private final AiProperties properties; private final ChatModelGateway chat; private final EmbeddingGateway embedding; private final VectorStorePort vectorStore;
    public AiAvailabilityService(AiProperties properties, ChatModelGateway chat, EmbeddingGateway embedding, VectorStorePort vectorStore) {
        this.properties = properties; this.chat = chat; this.embedding = embedding; this.vectorStore = vectorStore;
    }
    public AiOperationalState chat() { return !properties.isEnabled() ? AiOperationalState.DISABLED : chat instanceof DisabledChatModelGateway ? AiOperationalState.UNAVAILABLE : AiOperationalState.AVAILABLE; }
    public AiOperationalState rag() { return !properties.isEnabled() ? AiOperationalState.DISABLED : embedding instanceof DisabledEmbeddingGateway || vectorStore instanceof DisabledVectorStore ? AiOperationalState.UNAVAILABLE : AiOperationalState.AVAILABLE; }
}

