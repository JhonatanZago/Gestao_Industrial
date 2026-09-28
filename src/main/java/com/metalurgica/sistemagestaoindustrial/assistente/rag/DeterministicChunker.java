package com.metalurgica.sistemagestaoindustrial.assistente.rag;

import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeterministicChunker {
    private final AiProperties properties;

    public DeterministicChunker(AiProperties properties) {
        this.properties = properties;
    }

    public List<String> chunk(String text) {
        var chunks = new ArrayList<String>();
        var size = properties.getChunkSize();
        var overlap = properties.getChunkOverlap();
        for (int start = 0; start < text.length();) {
            var end = Math.min(text.length(), start + size);
            if (end < text.length()) {
                var boundary = text.lastIndexOf('\n', end);
                if (boundary > start + size / 2) end = boundary;
            }
            chunks.add(text.substring(start, end).trim());
            if (end >= text.length()) break;
            start = Math.max(start + 1, end - overlap);
        }
        return List.copyOf(chunks);
    }
}
