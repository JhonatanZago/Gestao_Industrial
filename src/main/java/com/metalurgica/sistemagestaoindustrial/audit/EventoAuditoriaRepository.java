package com.metalurgica.sistemagestaoindustrial.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long>, JpaSpecificationExecutor<EventoAuditoria> {
    Page<EventoAuditoria> findByEmpresaIdAndTipoRecursoAndRecursoId(Long empresaId, TipoRecursoAuditoria tipoRecurso,
                                                                      Long recursoId, Pageable pageable);
}
