package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Profile("test")
public class InMemoryVectorStore implements VectorStorePort {
    private final List<VectorDocument> documents = new CopyOnWriteArrayList<>();

    @Override
    public void upsert(VectorDocument document) {
        documents.removeIf(current -> current.chunkId().equals(document.chunkId()));
        documents.add(document);
    }

    @Override
    public List<VectorMatch> search(List<Double> vector, Map<String, Object> filters, int limit) {
        return documents.stream()
                .filter(document -> filters.entrySet().stream()
                        .allMatch(filter -> filter.getValue().equals(document.metadata().get(filter.getKey()))))
                .limit(Math.max(0, limit))
                .map(document -> new VectorMatch(document.chunkId(), document.text(), 1d, document.metadata()))
                .toList();
    }

    @Override
    public void delete(String documentId, String version) {
        documents.removeIf(document -> document.documentId().equals(documentId) && document.version().equals(version));
    }

    public List<VectorDocument> snapshot() {
        return new ArrayList<>(documents);
    }
}
