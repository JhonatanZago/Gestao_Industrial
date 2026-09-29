package com.metalurgica.sistemagestaoindustrial.observability;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.*;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AiAvailabilityServiceTests {
    @Test void disabledIsDistinctFromUnavailableAndAvailable() {
        var properties = new AiProperties();
        var service = new AiAvailabilityService(properties, new DisabledChatModelGateway(), new DisabledEmbeddingGateway(), new DisabledVectorStore());
        assertEquals(AiOperationalState.DISABLED, service.chat()); assertEquals(AiOperationalState.DISABLED, service.rag());
        properties.setEnabled(true);
        assertEquals(AiOperationalState.UNAVAILABLE, service.chat()); assertEquals(AiOperationalState.UNAVAILABLE, service.rag());
        service = new AiAvailabilityService(properties, request -> new ChatModelGateway.ChatModelResponse("ok", java.util.List.of(), 0, 0, "fake"),
                text -> new EmbeddingGateway.EmbeddingResponse(java.util.List.of(1D), 1, "fake"), new InMemoryVectorStore());
        assertEquals(AiOperationalState.AVAILABLE, service.chat()); assertEquals(AiOperationalState.AVAILABLE, service.rag());
    }
}
