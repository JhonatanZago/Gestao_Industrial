package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test & !local")
public class DisabledEmbeddingGateway implements EmbeddingGateway {
    @Override
    public EmbeddingResponse embed(String text) {
        throw new AiProviderException(AssistenteErrorCode.AI_PROVIDER_UNAVAILABLE,
                "Nenhum provider de embeddings está configurado.", false);
    }
}
