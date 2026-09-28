package com.metalurgica.sistemagestaoindustrial.assistente.planning;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteStatus;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.PeriodoAssistente;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AssistenteQuestionPlanner {
    private static final Pattern ID = Pattern.compile("(?:setor|funcion[aá]rio|funcionario|pe[cç]a|m[aá]quina|maquina)\\s*(?:id\\s*)?(\\d+)", Pattern.CASE_INSENSITIVE);
    private final AiProperties properties;

    public AssistenteQuestionPlanner(AiProperties properties) {
        this.properties = properties;
    }

    public Plan plan(String question, Clock clock) {
        if (question == null || question.isBlank()) return Plan.clarification("A pergunta não pode ser vazia.");
        var normalized = question.toLowerCase(Locale.ROOT);
        var operation = operation(normalized);
        var period = resolvePeriod(normalized, clock);
        if (operation == null) return Plan.clarification("Informe a métrica industrial que deseja consultar.");
        if (period == null && !operation.equals("rag_consulta")) return Plan.clarification("Informe um período, por exemplo ontem, esta semana ou datas ISO.");
        var args = new LinkedHashMap<String, Object>();
        if (period != null) {
            args.put("inicio", period.inicio().toString());
            args.put("fim", period.fim().toString());
        }
        addEntityId(normalized, args);
        if (normalized.contains("compar")) {
            if (period == null) return Plan.clarification("Informe um período para a comparação.");
            var duration = Duration.between(period.inicio(), period.fim()).plusSeconds(1);
            var previousStart = period.inicio().minus(duration);
            var previousEnd = period.fim().minus(duration);
            args.put("inicioAnterior", previousStart.toString());
            args.put("fimAnterior", previousEnd.toString());
            if (operation.equals("producao_resumo")) operation = "producao_comparacao";
            if (operation.equals("valor_industrial_resumo")) operation = "valor_industrial_comparacao";
        }
        return new Plan(AssistenteStatus.RESPONDIDO, null, operation, args, period);
    }

    private PeriodoAssistente resolvePeriod(String question, Clock clock) {
        var now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        var today = now.toLocalDate();
        if (question.contains("ontem")) {
            var day = today.minusDays(1);
            return day(day);
        }
        if (question.contains("hoje")) return day(today);
        if (question.contains("semana")) {
            var start = today.with(DayOfWeek.MONDAY).atStartOfDay();
            return new PeriodoAssistente(start, now);
        }
        if (question.contains("mês") || question.contains("mes")) {
            return new PeriodoAssistente(today.withDayOfMonth(1).atStartOfDay(), now);
        }
        var sevenDays = question.contains("7 dias") || question.contains("sete dias");
        if (sevenDays) return new PeriodoAssistente(now.minusDays(7), now);
        var dates = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})").matcher(question);
        if (dates.find()) {
            var start = LocalDate.parse(dates.group(1));
            if (dates.find()) return new PeriodoAssistente(start.atStartOfDay(), LocalDate.parse(dates.group(1)).atTime(23, 59, 59));
            return day(start);
        }
        return null;
    }

    private String operation(String question) {
        if (question.contains("manual") || question.contains("procedimento") || question.contains("instrução")
                || question.contains("instrucoes") || question.contains("documento")) return "rag_consulta";
        if (question.contains("auditoria")) return "auditoria_resumo";
        if (question.contains("qualidade") || question.contains("confiabilidade")) return "qualidade_resumo";
        if (question.contains("financeir") || question.contains("valor industrial")) return "valor_industrial_resumo";
        if (question.contains("parada") || question.contains("motivo")) return "paradas_por_motivo";
        if (question.contains("desempenho") || question.contains("meta")) return "desempenho_resumo";
        if (question.contains("produção") || question.contains("producao")) {
            if (question.contains("funcion")) return "producao_por_funcionario";
            if (question.contains("peça") || question.contains("peca")) return "producao_por_peca";
            if (question.contains("máquina") || question.contains("maquina")) return "producao_por_maquina";
            if (question.contains("setor")) return "producao_por_setor";
            return "producao_resumo";
        }
        return null;
    }

    private void addEntityId(String question, Map<String, Object> args) {
        Matcher matcher = ID.matcher(question);
        if (!matcher.find()) return;
        var value = Long.valueOf(matcher.group(1));
        if (question.contains("setor")) args.put("setorId", value);
        else if (question.contains("funcion")) args.put("funcionarioId", value);
        else if (question.contains("peça") || question.contains("peca")) args.put("pecaId", value);
        else if (question.contains("máquina") || question.contains("maquina")) args.put("maquinaId", value);
    }

    private PeriodoAssistente day(LocalDate day) {
        return new PeriodoAssistente(day.atStartOfDay(), day.atTime(23, 59, 59));
    }

    public record Plan(AssistenteStatus status, String clarification, String operation,
                       Map<String, Object> arguments, PeriodoAssistente period) {
        public Plan {
            arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        }

        static Plan clarification(String message) {
            return new Plan(AssistenteStatus.PRECISA_CLARIFICACAO, message, null, Map.of(), null);
        }
    }
}
