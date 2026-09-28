package com.metalurgica.sistemagestaoindustrial.audit;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.EnumMap;

@Component
public class AuditoriaSnapshotSanitizer {
    static final int MAX_BYTES = 32 * 1024;
    private static final Set<String> SENSITIVE_PARTS = Set.of(
            "senha", "password", "token", "secret", "authorization", "cookie",
            "credential", "credencial", "apikey", "api_key", "jwt", "hash"
    );
    private static final Set<String> COMMON_FIELDS = Set.of("id", "codigo", "nome", "descricao", "ativo", "status", "observacao", "dataHora", "metadados");
    private static final Map<TipoRecursoAuditoria, Set<String>> ALLOWED_FIELDS = allowedFields();

    public String sanitize(TipoRecursoAuditoria resource, Map<String, ?> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) return null;
        var clean = cleanMap(resource, snapshot);
        var json = toJson(clean);
        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new IllegalArgumentException("Snapshot de auditoria excede o limite permitido.");
        }
        return json;
    }

    private Map<String, Object> cleanMap(TipoRecursoAuditoria resource, Map<String, ?> values) {
        var allowed = ALLOWED_FIELDS.getOrDefault(resource, COMMON_FIELDS);
        var result = new LinkedHashMap<String, Object>();
        values.forEach((key, value) -> {
            if (key == null || sensitive(key) || (!allowed.contains(key) && !COMMON_FIELDS.contains(key))) return;
            result.put(key, cleanValue(value));
        });
        return result;
    }

    private Object cleanValue(Object value) {
        if (value instanceof Map<?, ?> nested) {
            var map = new LinkedHashMap<String, Object>();
            nested.forEach((key, nestedValue) -> {
                var name = key == null ? null : key.toString();
                if (name != null && !sensitive(name)) map.put(name, cleanValue(nestedValue));
            });
            return map;
        }
        if (value instanceof Collection<?> collection) return collection.stream().map(this::cleanValue).toList();
        return value;
    }

    private String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream().map(entry -> quote(String.valueOf(entry.getKey())) + ":" + toJson(entry.getValue()))
                    .collect(java.util.stream.Collectors.joining(",", "{", "}"));
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::toJson).collect(java.util.stream.Collectors.joining(",", "[", "]"));
        }
        if (value instanceof Number || value instanceof Boolean) return value instanceof BigDecimal decimal ? decimal.toPlainString() : value.toString();
        return quote(value.toString());
    }

    private String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"";
    }

    private boolean sensitive(String key) {
        var normalized = key.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_PARTS.stream().anyMatch(normalized::contains);
    }

    private static Map<TipoRecursoAuditoria, Set<String>> allowedFields() {
        var map = new EnumMap<TipoRecursoAuditoria, Set<String>>(TipoRecursoAuditoria.class);
        for (var resource : TipoRecursoAuditoria.values()) map.put(resource, COMMON_FIELDS);
        map.put(TipoRecursoAuditoria.REGISTRO_PRODUCAO, fields("id", "setorId", "funcionarioId", "pecaId", "maquinaId", "turnoId", "quantidade", "inicio", "fim", "observacao", "ativo", "origem", "importacaoId", "importacaoLinha"));
        map.put(TipoRecursoAuditoria.PARADA, fields("id", "registroProducaoId", "motivoParadaId", "maquinaId", "inicio", "fim", "observacao", "ativo"));
        map.put(TipoRecursoAuditoria.META_PRODUCAO, fields("id", "setorId", "pecaId", "tipo", "periodicidade", "quantidadeAlvo", "vigenciaInicio", "vigenciaFim", "ativo"));
        map.put(TipoRecursoAuditoria.TEMPO_PADRAO, fields("id", "setorId", "pecaId", "tempoSegundos", "vigenciaInicio", "vigenciaFim", "ativo"));
        map.put(TipoRecursoAuditoria.VALOR_PRODUCAO, fields("id", "setorId", "pecaId", "valor", "moeda", "tipoValor", "vigenciaInicio", "vigenciaFim", "ativo"));
        map.put(TipoRecursoAuditoria.USUARIO, fields("id", "nome", "email", "perfilId", "status", "ativo", "empresaId", "credenciaisVersao"));
        map.put(TipoRecursoAuditoria.PERFIL, fields("id", "codigo", "nome", "descricao", "permissoes"));
        map.put(TipoRecursoAuditoria.PERMISSAO, fields("id", "codigo", "nome", "descricao", "usuarioId", "perfilId", "concedida"));
        map.put(TipoRecursoAuditoria.IMPORTACAO, fields("id", "importacaoId", "arquivoNome", "setorId", "setor", "totalLinhas", "linhasValidas", "linhasInvalidas", "linhasImportadas", "quantidadeSucesso", "quantidadeErro", "status", "dataHora", "origem", "contexto"));
        map.put(TipoRecursoAuditoria.DOCUMENTO, fields("id", "documentId", "contentHash", "status"));
        return Map.copyOf(map);
    }

    private static Set<String> fields(String... names) { return Set.of(names); }
}
