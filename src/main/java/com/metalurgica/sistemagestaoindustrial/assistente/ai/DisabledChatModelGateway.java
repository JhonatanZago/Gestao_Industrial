package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test & !local")
public class DisabledChatModelGateway implements ChatModelGateway {
    @Override
    public ChatModelResponse complete(ChatModelRequest request) {
        throw new AiProviderException(AssistenteErrorCode.AI_PROVIDER_UNAVAILABLE,
                "Nenhum provider de IA está configurado.", false);
    }
}
