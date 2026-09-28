package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import java.util.List;

public interface EmbeddingGateway {
    EmbeddingResponse embed(String text);

    record EmbeddingResponse(List<Double> vector, int tokens, String model) {
        public EmbeddingResponse {
            vector = vector == null ? List.of() : List.copyOf(vector);
        }
    }
}
