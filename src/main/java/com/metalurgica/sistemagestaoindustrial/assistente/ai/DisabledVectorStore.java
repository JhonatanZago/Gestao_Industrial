package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnMissingBean(VectorStorePort.class)
public class DisabledVectorStore implements VectorStorePort {
    @Override public void upsert(VectorDocument document) { throw unavailable(); }
    @Override public List<VectorMatch> search(List<Double> vector, Map<String, Object> filters, int limit) { throw unavailable(); }
    @Override public void delete(String documentId, String version) { }
    private AiProviderException unavailable() { return new AiProviderException(AssistenteErrorCode.AI_PROVIDER_UNAVAILABLE, "Nenhum vector store está configurado.", false); }
}
