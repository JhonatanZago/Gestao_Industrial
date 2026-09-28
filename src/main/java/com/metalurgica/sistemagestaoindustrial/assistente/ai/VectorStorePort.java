package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import java.util.List;
import java.util.Map;

public interface VectorStorePort {
    void upsert(VectorDocument document);

    List<VectorMatch> search(List<Double> vector, Map<String, Object> filters, int limit);

    void delete(String documentId, String version);

    record VectorDocument(String chunkId, String documentId, String version,
                          List<Double> vector, String text, Map<String, Object> metadata) {
        public VectorDocument {
            vector = vector == null ? List.of() : List.copyOf(vector);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    record VectorMatch(String chunkId, String text, double score, Map<String, Object> metadata) {
        public VectorMatch {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }
}
