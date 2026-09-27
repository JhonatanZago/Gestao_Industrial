package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.RegistroProducao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RegistroProducaoRepository extends JpaRepository<RegistroProducao, Long>, JpaSpecificationExecutor<RegistroProducao> {
    Page<RegistroProducao> findByEmpresaId(Long empresaId, Pageable pageable);

    @Query("""
           select case when count(r) > 0 then true else false end
           from RegistroProducao r
           where r.funcionario.id = :funcionarioId
             and r.inicio < :fim
             and r.fim > :inicio
             and (:id is null or r.id <> :id)
           """)
    boolean existsOverlapping(@Param("funcionarioId") Long funcionarioId,
                              @Param("inicio") LocalDateTime inicio,
                              @Param("fim") LocalDateTime fim,
                              @Param("id") Long id);
}
