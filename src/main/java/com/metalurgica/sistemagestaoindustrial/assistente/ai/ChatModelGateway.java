package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import java.util.List;
import java.util.Map;

public interface ChatModelGateway {
    ChatModelResponse complete(ChatModelRequest request);

    record ChatModelRequest(
            List<ChatMessage> messages,
            List<ToolDefinition> tools,
            int maxOutputTokens) {
        public ChatModelRequest {
            messages = messages == null ? List.of() : List.copyOf(messages);
            tools = tools == null ? List.of() : List.copyOf(tools);
        }
    }

    record ChatMessage(String role, String content) {
    }

    record ToolDefinition(String name, String description, Map<String, Object> inputSchema) {
        public ToolDefinition {
            inputSchema = inputSchema == null ? Map.of() : Map.copyOf(inputSchema);
        }
    }

    record ChatModelResponse(
            String answer,
            List<ToolCall> toolCalls,
            int inputTokens,
            int outputTokens,
            String model) {
        public ChatModelResponse {
            toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        }
    }

    record ToolCall(String name, Map<String, Object> arguments) {
        public ToolCall {
            arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        }
    }
}
