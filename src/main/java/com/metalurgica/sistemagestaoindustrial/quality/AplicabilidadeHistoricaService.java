package com.metalurgica.sistemagestaoindustrial.quality;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class AplicabilidadeHistoricaService {
    public TempoPadraoProducao tempoPadrao(List<TempoPadraoProducao> candidatos, RegistroProducao producao) {
        if (producao.getInicio() == null || producao.getPeca() == null || producao.getSetor() == null) return null;
        var data = producao.getInicio().toLocalDate();
        return candidatos.stream()
                .filter(t -> Objects.equals(t.getPeca().getId(), producao.getPeca().getId()))
                .filter(t -> Objects.equals(t.getSetor().getId(), producao.getSetor().getId()))
                .filter(t -> vigenciaContem(t.getVigenciaInicio(), t.getVigenciaFim(), data))
                .filter(t -> t.getMaquina() == null || (producao.getMaquina() != null && Objects.equals(t.getMaquina().getId(), producao.getMaquina().getId())))
                .sorted(Comparator.comparing((TempoPadraoProducao t) -> t.getMaquina() == null ? 0 : 1).reversed()
                        .thenComparing(TempoPadraoProducao::getVigenciaInicio, Comparator.reverseOrder()))
                .findFirst().orElse(null);
    }

    public List<ValorProducao> valores(List<ValorProducao> candidatos, RegistroProducao producao) {
        if (producao.getInicio() == null || producao.getPeca() == null || producao.getSetor() == null) return List.of();
        var data = producao.getInicio().toLocalDate();
        return candidatos.stream()
                .filter(v -> Objects.equals(v.getPeca().getId(), producao.getPeca().getId()))
                .filter(v -> Objects.equals(v.getSetor().getId(), producao.getSetor().getId()))
                .filter(v -> vigenciaContem(v.getVigenciaInicio(), v.getVigenciaFim(), data))
                .filter(v -> v.getMaquina() == null || (producao.getMaquina() != null && Objects.equals(v.getMaquina().getId(), producao.getMaquina().getId())))
                .collect(java.util.stream.Collectors.groupingBy(v -> v.getMoeda() + ":" + v.getTipoValor()))
                .values().stream()
                .map(lista -> lista.stream()
                        .filter(v -> v.getMaquina() != null && producao.getMaquina() != null && Objects.equals(v.getMaquina().getId(), producao.getMaquina().getId()))
                        .max(Comparator.comparing(ValorProducao::getVigenciaInicio))
                        .orElseGet(() -> lista.stream().filter(v -> v.getMaquina() == null)
                                .max(Comparator.comparing(ValorProducao::getVigenciaInicio)).orElse(null)))
                .filter(Objects::nonNull).toList();
    }

    public MetaProducao meta(List<MetaProducao> candidatos, RegistroProducao producao) {
        if (producao.getInicio() == null) return null;
        var data = producao.getInicio().toLocalDate();
        return candidatos.stream().filter(m -> vigenciaContem(m.getVigenciaInicio(), m.getVigenciaFim(), data))
                .filter(m -> switch (m.getTipo()) {
                    case EMPRESA -> true;
                    case SETOR -> same(m.getSetor(), producao.getSetor());
                    case FUNCIONARIO -> same(m.getFuncionario(), producao.getFuncionario());
                    case PECA -> same(m.getPeca(), producao.getPeca());
                    case MAQUINA -> same(m.getMaquina(), producao.getMaquina());
                    case TURNO -> same(m.getTurno(), producao.getTurno());
                    case COMPOSTA -> (m.getSetor() == null || same(m.getSetor(), producao.getSetor()))
                            && (m.getFuncionario() == null || same(m.getFuncionario(), producao.getFuncionario()))
                            && (m.getPeca() == null || same(m.getPeca(), producao.getPeca()))
                            && (m.getMaquina() == null || same(m.getMaquina(), producao.getMaquina()))
                            && (m.getTurno() == null || same(m.getTurno(), producao.getTurno()));
                })
                .sorted(Comparator.comparingInt(this::specificity).reversed()
                        .thenComparing(MetaProducao::getVigenciaInicio, Comparator.reverseOrder()))
                .findFirst().orElse(null);
    }

    public boolean vigenciaContem(LocalDate inicio, LocalDate fim, LocalDate data) {
        return inicio != null && !inicio.isAfter(data) && (fim == null || !fim.isBefore(data));
    }

    private boolean same(Object left, Object right) { return left != null && right != null && left.equals(right); }

    private int specificity(MetaProducao meta) {
        return switch (meta.getTipo()) {
            case EMPRESA -> 0;
            case COMPOSTA -> 5;
            default -> 1;
        };
    }
}
