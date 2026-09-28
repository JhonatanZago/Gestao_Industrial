package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.MetaProducao;
import com.metalurgica.sistemagestaoindustrial.domain.TipoMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MetaProducaoRepository extends JpaRepository<MetaProducao, Long>, JpaSpecificationExecutor<MetaProducao> {
    Optional<MetaProducao> findByIdAndEmpresaId(Long id, Long empresaId);
    Page<MetaProducao> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<MetaProducao> findByEmpresaIdAndAtivo(Long empresaId, boolean ativo, Pageable pageable);
    Page<MetaProducao> findByEmpresaIdAndTipo(Long empresaId, TipoMeta tipo, Pageable pageable);

    @Query("""
           select m from MetaProducao m
           where m.empresa.id = :empresaId and m.ativo = true
             and m.vigenciaInicio <= :data
             and (m.vigenciaFim is null or m.vigenciaFim >= :data)
           order by m.vigenciaInicio desc
           """)
    List<MetaProducao> findApplicable(@Param("empresaId") Long empresaId, @Param("data") LocalDate data);

    @Query("select m from MetaProducao m where m.empresa.id = :empresaId and m.ativo = true and m.vigenciaInicio <= :fim and (m.vigenciaFim is null or m.vigenciaFim >= :inicio)")
    @EntityGraph(attributePaths = {"setor", "funcionario", "peca", "maquina", "turno"})
    List<MetaProducao> findActiveForPeriod(@Param("empresaId") Long empresaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
           select case when count(m) > 0 then true else false end from MetaProducao m
           where m.empresa.id = :empresaId and m.tipo = :tipo and m.periodicidade = :periodicidade
             and ((m.setor.id = :setorId) or (m.setor is null and :setorId is null))
             and ((m.funcionario.id = :funcionarioId) or (m.funcionario is null and :funcionarioId is null))
             and ((m.peca.id = :pecaId) or (m.peca is null and :pecaId is null))
             and ((m.maquina.id = :maquinaId) or (m.maquina is null and :maquinaId is null))
             and ((m.turno.id = :turnoId) or (m.turno is null and :turnoId is null))
             and m.ativo = true and (:id is null or m.id <> :id)
             and m.vigenciaInicio <= coalesce(:fim, m.vigenciaInicio)
             and (m.vigenciaFim is null or m.vigenciaFim >= :inicio)
           """)
    boolean existsConflict(@Param("empresaId") Long empresaId, @Param("tipo") TipoMeta tipo,
                           @Param("periodicidade") com.metalurgica.sistemagestaoindustrial.domain.PeriodicidadeMeta periodicidade,
                           @Param("setorId") Long setorId, @Param("funcionarioId") Long funcionarioId,
                           @Param("pecaId") Long pecaId, @Param("maquinaId") Long maquinaId,
                           @Param("turnoId") Long turnoId, @Param("inicio") LocalDate inicio,
                           @Param("fim") LocalDate fim, @Param("id") Long id);
}
