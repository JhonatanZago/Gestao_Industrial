package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(ChatModelGateway.class)
public class DisabledChatModelGateway implements ChatModelGateway {
    @Override
    public ChatModelResponse complete(ChatModelRequest request) {
        throw new AiProviderException(AssistenteErrorCode.AI_PROVIDER_UNAVAILABLE,
                "Nenhum provider de IA está configurado.", false);
    }
}
