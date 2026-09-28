package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(EmbeddingGateway.class)
public class DisabledEmbeddingGateway implements EmbeddingGateway {
    @Override
    public EmbeddingResponse embed(String text) {
        throw new AiProviderException(AssistenteErrorCode.AI_PROVIDER_UNAVAILABLE,
                "Nenhum provider de embeddings está configurado.", false);
    }
}
