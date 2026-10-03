package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.importacao.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImportacaoValidator {
    private final EmpresaService empresaService;
    private final SetorRepository setorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PecaRepository pecaRepository;
    private final MaquinaRepository maquinaRepository;
    private final TurnoRepository turnoRepository;
    private final RegistroProducaoRepository producaoRepository;

    public ImportacaoValidator(EmpresaService empresaService, SetorRepository setorRepository,
                               FuncionarioRepository funcionarioRepository, PecaRepository pecaRepository,
                               MaquinaRepository maquinaRepository, TurnoRepository turnoRepository,
                               RegistroProducaoRepository producaoRepository) {
        this.empresaService = empresaService;
        this.setorRepository = setorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.pecaRepository = pecaRepository;
        this.maquinaRepository = maquinaRepository;
        this.turnoRepository = turnoRepository;
        this.producaoRepository = producaoRepository;
    }

    @Transactional(readOnly = true)
    public ValidacaoImportacaoResult validate(Long empresaId, Long setorId, ExcelReadResult input) {
        empresaService.find(empresaId);
        var setor = setorRepository.findByIdAndEmpresaId(setorId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Setor nao encontrado nesta empresa."));
        if (!setor.isAtivo()) throw new IllegalArgumentException("SETOR_INATIVO");

        var errors = new ArrayList<>(input.erros());
        var rows = input.linhas();
        var funcs = funcionarioRepository.findByEmpresaIdAndCodigoIn(empresaId,
                        rows.stream().map(row -> code(row.codigoFuncionario())).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(f -> code(f.getCodigo()), f -> f, (a, b) -> a));
        var pecas = pecaRepository.findByEmpresaIdAndCodigoIn(empresaId,
                        rows.stream().map(row -> code(row.codigoPeca())).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(p -> code(p.getCodigo()), p -> p, (a, b) -> a));
        var valid = new ArrayList<LinhaValidadaImportacao>();
        var signatures = new HashMap<String, Integer>();
        var byEmployee = new HashMap<Long, List<LinhaValidadaImportacao>>();
        for (var row : rows) {
            var func = funcs.get(code(row.codigoFuncionario()));
            var peca = pecas.get(code(row.codigoPeca()));
            boolean bad = false;
            if (func == null) { errors.add(err(row, "codigoFuncionario", "FUNCIONARIO_NAO_ENCONTRADO", "Funcionario nao encontrado.")); bad = true; }
            else if (!func.isAtivo()) { errors.add(err(row, "codigoFuncionario", "FUNCIONARIO_INATIVO", "Funcionario inativo.")); bad = true; }
            if (peca == null) { errors.add(err(row, "codigoPeca", "PECA_NAO_ENCONTRADA", "Peca nao encontrada.")); bad = true; }
            else if (!peca.isAtivo()) { errors.add(err(row, "codigoPeca", "PECA_INATIVA", "Peca inativa.")); bad = true; }
            if (!isPositiveInteger(row.quantidade())) { errors.add(err(row, "quantidade", "QUANTIDADE_INVALIDA", "Quantidade deve ser um inteiro maior que zero.")); bad = true; }

            // Temporariamente, máquina e turno não fazem parte da validação do fluxo Excel.
            // Os campos continuam sendo aceitos pelo template, mas permanecem nulos até a
            // homologação desses cadastros ser reativada.
            Maquina machine = null;
            Turno shift = null;

            LocalDateTime start = row.inicioDateTime();
            LocalDateTime end = row.fimDateTime(false);
            if (start == null || end == null || !end.isAfter(start)) {
                errors.add(err(row, "periodo", "PERIODO_INVALIDO", "Hora fim deve ser posterior a hora inicio.")); bad = true;
            }
            if (bad) continue;

            String signature = code(row.codigoFuncionario()) + "|" + code(row.codigoPeca()) + "|" + row.quantidade() + "|" + start + "|" + end;
            if (signatures.containsKey(signature)) {
                errors.add(err(row, "linha", "LINHA_DUPLICADA", "Linha duplicada da linha " + signatures.get(signature) + "."));
                continue;
            }
            signatures.put(signature, row.linha());
            if (producaoRepository.existsOverlapping(func.getId(), start, end, null)) {
                errors.add(err(row, "periodo", "SOBREPOSICAO", "Periodo sobreposto a uma producao existente."));
                continue;
            }
            var line = new LinhaValidadaImportacao(row, func, peca, machine, shift, start, end);
            boolean overlaps = false;
            for (var prior : byEmployee.getOrDefault(func.getId(), List.of())) {
                if (prior.inicio().isBefore(end) && prior.fim().isAfter(start)) {
                    errors.add(err(row, "periodo", "SOBREPOSICAO", "Periodo sobreposto a linha " + prior.linha().linha() + "."));
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                valid.add(line);
                byEmployee.computeIfAbsent(func.getId(), ignored -> new ArrayList<>()).add(line);
            }
        }
        return new ValidacaoImportacaoResult(valid, errors, input.totalLinhas());
    }

    private static boolean isPositiveInteger(BigDecimal value) {
        return value != null && value.signum() > 0 && value.stripTrailingZeros().scale() <= 0;
    }

    private static ImportacaoLinhaErro err(LinhaProducaoImportada row, String field, String code, String message) {
        return new ImportacaoLinhaErro(row.linha(), field, code, message, row.codigoFuncionario(), row.codigoPeca());
    }

    private static String code(String value) {
        return value == null ? null : EmpresaService.normalizeText(value).toUpperCase(Locale.ROOT);
    }
}
