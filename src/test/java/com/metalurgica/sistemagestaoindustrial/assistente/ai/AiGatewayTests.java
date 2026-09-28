package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiGatewayTests {
    @Test
    void fakeEmbeddingEIdempotenteParaMesmoTexto() {
        var gateway = new FakeEmbeddingGateway();
        assertEquals(gateway.embed("abc"), gateway.embed("abc"));
    }

    @Test
    void vectorStoreAplicaFiltrosAntesDoResultado() {
        var store = new InMemoryVectorStore();
        store.upsert(new VectorStorePort.VectorDocument("c1", "d1", "v1", java.util.List.of(1d),
                "setor A", Map.of("empresaId", 1L, "sectorId", 10L)));
        store.upsert(new VectorStorePort.VectorDocument("c2", "d2", "v1", java.util.List.of(1d),
                "setor B", Map.of("empresaId", 2L, "sectorId", 10L)));
        assertEquals(1, store.search(java.util.List.of(1d), Map.of("empresaId", 1L), 10).size());
    }
}
