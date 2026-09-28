package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.ParadaProducao;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;

public interface ParadaProducaoRepository extends JpaRepository<ParadaProducao, Long> {
    Optional<ParadaProducao> findByIdAndEmpresaIdAndRegistroProducaoId(Long id, Long empresaId, Long producaoId);
    Optional<ParadaProducao> findByIdAndEmpresaId(Long id, Long empresaId);
    Page<ParadaProducao> findByEmpresaIdAndRegistroProducaoId(Long empresaId, Long producaoId, Pageable pageable);
    List<ParadaProducao> findByRegistroProducaoIdInAndAtivoTrue(Collection<Long> producaoIds);
    @Query("""
           select case when count(p) > 0 then true else false end from ParadaProducao p
           where p.registroProducao.id = :producaoId and p.ativo = true
             and p.inicio < :fim and p.fim > :inicio and (:id is null or p.id <> :id)
           """)
    boolean existsOverlapping(@Param("producaoId") Long producaoId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, @Param("id") Long id);
    @Query("select count(p) > 0 from ParadaProducao p where p.registroProducao.id = :producaoId and (p.inicio < :inicio or p.fim > :fim)")
    boolean existsOutsidePeriod(@Param("producaoId") Long producaoId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
