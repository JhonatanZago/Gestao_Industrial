package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsQuery;
import com.metalurgica.sistemagestaoindustrial.exception.AnalyticsPeriodException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;

@Service
public class AnalyticsQueryValidator {
    private final EmpresaService empresaService;
    private final SetorRepository setorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PecaRepository pecaRepository;
    private final MaquinaRepository maquinaRepository;
    private final TurnoRepository turnoRepository;
    private final SectorScopeService sectorScopeService;

    public AnalyticsQueryValidator(EmpresaService empresaService, SetorRepository setorRepository,
                                   FuncionarioRepository funcionarioRepository, PecaRepository pecaRepository,
                                   MaquinaRepository maquinaRepository, TurnoRepository turnoRepository,
                                   SectorScopeService sectorScopeService) {
        this.empresaService = empresaService;
        this.setorRepository = setorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.pecaRepository = pecaRepository;
        this.maquinaRepository = maquinaRepository;
        this.turnoRepository = turnoRepository;
        this.sectorScopeService = sectorScopeService;
    }

    @Transactional(readOnly = true)
    public void validate(Long empresaId, AnalyticsQuery query) {
        empresaService.find(empresaId);
        if (query == null || query.inicio() == null || query.fim() == null) {
            throw new AnalyticsPeriodException("O período analítico exige inicio e fim.");
        }
        if (query.fim().isBefore(query.inicio())) {
            throw new AnalyticsPeriodException("O fim do período analítico deve ser posterior ou igual ao início.");
        }
        ensureContext(query.setorId(), id -> setorRepository.findByIdAndEmpresaId(id, empresaId).isPresent(), "Setor");
        sectorScopeService.assertAllowed(empresaId, query.setorId());
        ensureContext(query.funcionarioId(), id -> funcionarioRepository.findByIdAndEmpresaId(id, empresaId).isPresent(), "Funcionário");
        ensureContext(query.pecaId(), id -> pecaRepository.findByIdAndEmpresaId(id, empresaId).isPresent(), "Peça");
        ensureContext(query.maquinaId(), id -> maquinaRepository.findByIdAndEmpresaId(id, empresaId).isPresent(), "Máquina");
        ensureContext(query.turnoId(), id -> turnoRepository.findByIdAndEmpresaId(id, empresaId).isPresent(), "Turno");
    }

    private static void ensureContext(Long id, java.util.function.Predicate<Long> exists, String name) {
        if (id != null && !exists.test(id)) {
            throw new ResourceNotFoundException(name + " não encontrado no contexto da Empresa.");
        }
    }
}
