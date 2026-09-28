package com.metalurgica.sistemagestaoindustrial.assistente.tool;

import java.util.List;

public interface AiToolCatalog {
    List<AiToolDefinition> definitions();

    AiToolResult execute(AiToolRequest request);
}
