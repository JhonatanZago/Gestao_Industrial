package com.metalurgica.sistemagestaoindustrial.analytics;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.dto.response.ProducaoAgrupadaResponse;

import java.math.BigDecimal;

final class AnalyticsAggregate {
    private final Long id;
    private final String codigo;
    private final String nome;
    private final String setorCodigo;
    private final String setorNome;
    private BigDecimal quantidade = BigDecimal.ZERO;
    private long registros;
    private long duracao;
    private long parado;

    private AnalyticsAggregate(Long id, String codigo, String nome, String setorCodigo, String setorNome) {
        this.id = id; this.codigo = codigo; this.nome = nome; this.setorCodigo = setorCodigo; this.setorNome = setorNome;
    }

    static AnalyticsAggregate forRecord(AnalyticsRecord record, Dimension dimension) {
        var r = record.producao();
        return switch (dimension) {
            case SETOR -> new AnalyticsAggregate(r.getSetor().getId(), r.getSetor().getCodigo(), r.getSetor().getNome(), r.getSetor().getCodigo(), r.getSetor().getNome());
            case FUNCIONARIO -> new AnalyticsAggregate(r.getFuncionario().getId(), r.getFuncionario().getCodigo(), r.getFuncionario().getNome(), r.getSetor().getCodigo(), r.getSetor().getNome());
            case PECA -> new AnalyticsAggregate(r.getPeca().getId(), r.getPeca().getCodigo(), r.getPeca().getDescricao(), r.getSetor().getCodigo(), r.getSetor().getNome());
            case MAQUINA -> r.getMaquina() == null ? new AnalyticsAggregate(null, "SEM_MAQUINA", "Sem máquina", r.getSetor().getCodigo(), r.getSetor().getNome()) : new AnalyticsAggregate(r.getMaquina().getId(), r.getMaquina().getCodigo(), r.getMaquina().getNome(), r.getSetor().getCodigo(), r.getSetor().getNome());
            case TURNO -> r.getTurno() == null ? new AnalyticsAggregate(null, "SEM_TURNO", "Sem turno", r.getSetor().getCodigo(), r.getSetor().getNome()) : new AnalyticsAggregate(r.getTurno().getId(), r.getTurno().getCodigo(), r.getTurno().getNome(), r.getSetor().getCodigo(), r.getSetor().getNome());
        };
    }

    void add(AnalyticsRecord record) {
        quantidade = quantidade.add(record.quantidade()); registros++; duracao += record.duracaoBrutaSegundos(); parado += record.tempoParadoSegundos();
    }

    ProducaoAgrupadaResponse response() {
        var m = AnalyticsMetrics.from(quantidade, registros, duracao, parado);
        return new ProducaoAgrupadaResponse(id, codigo, nome, setorCodigo, setorNome, m.quantidadeProduzida(), m.quantidadeRegistros(), m.duracaoBrutaMinutos(), m.tempoParadoMinutos(), m.tempoProdutivoMinutos(), m.taxaProducaoBruta(), m.taxaProducaoProdutiva(), m.percentualTempoParado());
    }
}
