package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Profile("test | local")
public class FakeEmbeddingGateway implements EmbeddingGateway {
    @Override
    public EmbeddingResponse embed(String text) {
        var bytes = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        var sum = 0d;
        for (byte value : bytes) sum += Byte.toUnsignedInt(value);
        return new EmbeddingResponse(List.of((double) bytes.length, sum), bytes.length, "fake");
    }
}
