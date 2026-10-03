package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile("test | local")
public class FakeChatModelGateway implements ChatModelGateway {
    private final AtomicInteger calls = new AtomicInteger();

    @Override
    public ChatModelResponse complete(ChatModelRequest request) {
        calls.incrementAndGet();
        return new ChatModelResponse("Resposta offline determinística.", java.util.List.of(), 0, 4, "fake");
    }

    public int calls() {
        return calls.get();
    }
}
