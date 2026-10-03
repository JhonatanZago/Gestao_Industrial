package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.assistente.ai.ChatModelGateway;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.DisabledChatModelGateway;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.DisabledEmbeddingGateway;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.DisabledVectorStore;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.EmbeddingGateway;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.VectorStorePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:prod-ai-fallback;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=true",
    "security.jwt.secret=prod-ai-fallback-test-secret-key-with-32-bytes",
    "ai.enabled=false"
})
@ActiveProfiles("prod")
class ProductionAiFallbackContextTests {
    @Autowired EmbeddingGateway embeddingGateway;
    @Autowired ChatModelGateway chatModelGateway;
    @Autowired VectorStorePort vectorStore;

    @Test
    void productionProfileProvidesDisabledAiFallbackBeans() {
        assertInstanceOf(DisabledEmbeddingGateway.class, embeddingGateway);
        assertInstanceOf(DisabledChatModelGateway.class, chatModelGateway);
        assertInstanceOf(DisabledVectorStore.class, vectorStore);
    }
}
