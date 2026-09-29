package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.dto.response.ImportacaoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.importacao.*;
import com.metalurgica.sistemagestaoindustrial.mapper.ImportacaoProducaoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportacaoLifecycleService {
    private final ImportacaoProducaoRepository repository;
    private final ResultadoLinhaImportacaoRepository resultadoRepository;
    private final EmpresaRepository empresaRepository;
    private final SetorRepository setorRepository;
    private final RegistroProducaoRepository producaoRepository;
    private final AuditoriaService auditoriaService;
    private final ImportacaoProducaoMapper mapper;

    public ImportacaoLifecycleService(ImportacaoProducaoRepository repository,
                                      ResultadoLinhaImportacaoRepository resultadoRepository,
                                      EmpresaRepository empresaRepository,
                                      SetorRepository setorRepository,
                                      RegistroProducaoRepository producaoRepository,
                                      AuditoriaService auditoriaService,
                                      ImportacaoProducaoMapper mapper) {
        this.repository = repository;
        this.resultadoRepository = resultadoRepository;
        this.empresaRepository = empresaRepository;
        this.setorRepository = setorRepository;
        this.producaoRepository = producaoRepository;
        this.auditoriaService = auditoriaService;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long create(Long empresaId, Long setorId, String nomeArquivo, String hash, String correlationId) {
        var empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));
        var setor = setorRepository.findByIdAndEmpresaId(setorId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa."));
        var importacao = new ImportacaoProducao();
        importacao.setEmpresa(empresa);
        importacao.setSetor(setor);
        importacao.setNomeArquivo(nomeArquivo);
        importacao.setHashArquivo(hash);
        importacao.setCorrelationId(correlationId);
        importacao.touchCreate();
        importacao.setStatus(StatusImportacao.VALIDANDO);
        return repository.saveAndFlush(importacao).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportacaoProducaoResponse reject(Long id, ValidacaoImportacaoResult validation,
                                             String errorCode, String errorMessage) {
        var importacao = required(id);
        applyTotals(importacao, validation);
        importacao.setStatus(StatusImportacao.REJEITADA);
        importacao.setFinalizadoEm(OffsetDateTime.now());
        importacao.setErroCodigo(errorCode);
        importacao.setErroMensagem(errorMessage);
        saveErrors(importacao, validation.erros());
        auditImport(importacao);
        return mapper.toResponse(repository.save(importacao));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportacaoProducaoResponse validated(Long id, ValidacaoImportacaoResult validation) {
        var importacao = required(id);
        applyTotals(importacao, validation);
        importacao.setStatus(StatusImportacao.VALIDADA);
        importacao.setFinalizadoEm(OffsetDateTime.now());
        auditImport(importacao);
        return mapper.toResponse(repository.save(importacao));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportacaoProducaoResponse complete(Long id, ValidacaoImportacaoResult validation) {
        var importacao = required(id);
        applyTotals(importacao, validation);
        importacao.setStatus(StatusImportacao.IMPORTANDO);
        repository.save(importacao);
        for (var line : validation.validas()) {
            var registro = new RegistroProducao();
            registro.setEmpresa(importacao.getEmpresa());
            registro.setSetor(importacao.getSetor());
            registro.setFuncionario(line.funcionario());
            registro.setPeca(line.peca());
            registro.setMaquina(line.maquina());
            registro.setTurno(line.turno());
            registro.setQuantidade(line.quantidade());
            registro.setInicio(line.inicio());
            registro.setFim(line.fim());
            registro.setOrigem(OrigemProducao.IMPORTACAO);
            registro.setImportacao(importacao);
            registro.setImportacaoLinha(line.linha().linha());
            registro.touchForCreate();
            var saved = producaoRepository.save(registro);
            auditoriaService.record(importacao.getEmpresa().getId(), TipoRecursoAuditoria.REGISTRO_PRODUCAO,
                    saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.IMPORTACAO_EXCEL, null,
                    productionSnapshot(saved), Map.of("setorId", importacao.getSetor().getId(),
                            "importacaoId", importacao.getId()), importacao.getCorrelationId());
        }
        importacao.setLinhasImportadas(validation.validas().size());
        importacao.setStatus(StatusImportacao.CONCLUIDA);
        importacao.setFinalizadoEm(OffsetDateTime.now());
        auditImport(importacao);
        return mapper.toResponse(repository.saveAndFlush(importacao));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long id, String errorCode, String errorMessage) {
        var importacao = required(id);
        importacao.setStatus(StatusImportacao.FALHOU);
        importacao.setFinalizadoEm(OffsetDateTime.now());
        importacao.setErroCodigo(errorCode);
        importacao.setErroMensagem(errorMessage);
        auditImport(importacao);
        repository.saveAndFlush(importacao);
    }

    private ImportacaoProducao required(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Importação não encontrada."));
    }

    private static void applyTotals(ImportacaoProducao importacao, ValidacaoImportacaoResult validation) {
        importacao.setTotalLinhas(validation.totalLinhas());
        importacao.setLinhasValidas(validation.validas().size());
        importacao.setLinhasInvalidas(validation.erros().size());
    }

    private void saveErrors(ImportacaoProducao importacao, List<ImportacaoLinhaErro> errors) {
        resultadoRepository.saveAll(errors.stream().map(error -> {
            var result = new ResultadoLinhaImportacao();
            result.setImportacao(importacao);
            result.setNumeroLinha(error.linha());
            result.setStatus("ERRO");
            result.setCodigoFuncionario(error.codigoFuncionario());
            result.setCodigoPeca(error.codigoPeca());
            result.setCodigoErro(error.codigo());
            result.setMensagem(error.mensagem());
            return result;
        }).toList());
    }

    private void auditImport(ImportacaoProducao importacao) {
        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("importacaoId", importacao.getId());
        metadata.put("arquivoNome", importacao.getNomeArquivo());
        metadata.put("setorId", importacao.getSetor().getId());
        metadata.put("totalLinhas", importacao.getTotalLinhas());
        metadata.put("linhasValidas", importacao.getLinhasValidas());
        metadata.put("linhasInvalidas", importacao.getLinhasInvalidas());
        metadata.put("linhasImportadas", importacao.getLinhasImportadas());
        metadata.put("status", importacao.getStatus());
        metadata.put("dataHora", importacao.getFinalizadoEm());
        metadata.put("erroCodigo", importacao.getErroCodigo());
        auditoriaService.record(importacao.getEmpresa().getId(), TipoRecursoAuditoria.IMPORTACAO,
                importacao.getId(), AcaoAuditoria.IMPORTACAO, OrigemAuditoria.IMPORTACAO_EXCEL,
                null, metadata, metadata, importacao.getCorrelationId());
    }

    private static Map<String, Object> productionSnapshot(RegistroProducao registro) {
        var values = new LinkedHashMap<String, Object>();
        values.put("id", registro.getId());
        values.put("setorId", registro.getSetor().getId());
        values.put("funcionarioId", registro.getFuncionario().getId());
        values.put("pecaId", registro.getPeca().getId());
        values.put("maquinaId", registro.getMaquina() == null ? null : registro.getMaquina().getId());
        values.put("turnoId", registro.getTurno() == null ? null : registro.getTurno().getId());
        values.put("quantidade", registro.getQuantidade());
        values.put("inicio", registro.getInicio());
        values.put("fim", registro.getFim());
        values.put("ativo", registro.isAtivo());
        values.put("origem", registro.getOrigem());
        values.put("importacaoId", registro.getImportacao().getId());
        values.put("importacaoLinha", registro.getImportacaoLinha());
        return values;
    }
}
