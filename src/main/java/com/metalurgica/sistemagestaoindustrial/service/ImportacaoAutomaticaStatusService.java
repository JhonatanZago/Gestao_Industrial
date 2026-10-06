package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.dto.response.ImportacaoAutomaticaStatusResponse;
import com.metalurgica.sistemagestaoindustrial.importacao.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ImportacaoAutomaticaStatusService {
    private final AutomaticImportProperties properties;
    private final ServidorExcelWatcher watcher;
    private final ImportacaoAutomaticaHealthService health;
    private final ImportacaoProducaoRepository imports;
    private final SetorRepository setores;
    private final EmpresaService empresas;

    public ImportacaoAutomaticaStatusService(AutomaticImportProperties properties, ServidorExcelWatcher watcher,
            ImportacaoAutomaticaHealthService health, ImportacaoProducaoRepository imports,
            SetorRepository setores, EmpresaService empresas) {
        this.properties = properties; this.watcher = watcher; this.health = health;
        this.imports = imports; this.setores = setores; this.empresas = empresas;
    }

    public ImportacaoAutomaticaStatusResponse status(Long empresaId) {
        empresas.find(empresaId);
        var sources = health.check().stream().filter(s -> Objects.equals(s.empresaId(), empresaId)).toList();
        boolean active = properties.isEnabled() && watcher.isRunning() && !sources.isEmpty() && sources.stream().allMatch(ImportacaoAutomaticaHealthService.SourceHealth::acessivel);
        String global = !properties.isEnabled() ? "DESATIVADO" : active ? "ATIVO" : "ATENCAO";
        var overall = imports.findTopByEmpresaIdOrderByCreatedAtDesc(empresaId).map(this::summary).orElse(null);
        var fonts = sources.stream().map(source -> {
            var name = setores.findByIdAndEmpresaId(source.setorId(), empresaId).map(s -> s.getNome()).orElse("Setor " + source.setorId());
            var latest = imports.findTopByEmpresaIdAndSetorIdOrderByCreatedAtDesc(empresaId, source.setorId()).map(this::summary).orElse(null);
            String state = !properties.isEnabled() ? "INATIVA" : !source.acessivel() ? "INDISPONIVEL" : watcher.isRunning() ? "ONLINE" : "ERRO";
            return new ImportacaoAutomaticaStatusResponse.Fonte(source.setorId(), name, properties.isEnabled(), source.acessivel(), state, source.ultimoCheck(), latest);
        }).toList();
        var r = properties.getReconciliation();
        return new ImportacaoAutomaticaStatusResponse(properties.isEnabled(), watcher.isRunning(), global,
                health.lastCheck(), overall, new ImportacaoAutomaticaStatusResponse.Reconciliacao(r.isEnabled(), r.getCron(), r.getZone()), fonts);
    }

    private ImportacaoAutomaticaStatusResponse.ImportacaoResumo summary(com.metalurgica.sistemagestaoindustrial.domain.ImportacaoProducao i) {
        return new ImportacaoAutomaticaStatusResponse.ImportacaoResumo(i.getId(), i.getNomeArquivo(), i.getOrigem().name(), i.getStatus().name(), i.getIniciadoEm(), i.getFinalizadoEm(), i.getTotalLinhas(), i.getLinhasImportadas(), i.getLinhasIgnoradas());
    }
}
