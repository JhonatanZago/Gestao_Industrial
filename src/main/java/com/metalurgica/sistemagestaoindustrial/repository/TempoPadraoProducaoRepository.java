package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.TempoPadraoProducao;
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

public interface TempoPadraoProducaoRepository extends JpaRepository<TempoPadraoProducao, Long>, JpaSpecificationExecutor<TempoPadraoProducao> {
    Optional<TempoPadraoProducao> findByIdAndEmpresaId(Long id, Long empresaId);
    Page<TempoPadraoProducao> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<TempoPadraoProducao> findByEmpresaIdAndAtivo(Long empresaId, boolean ativo, Pageable pageable);
    Page<TempoPadraoProducao> findByEmpresaIdAndPecaId(Long empresaId, Long pecaId, Pageable pageable);
    Page<TempoPadraoProducao> findByEmpresaIdAndSetorId(Long empresaId, Long setorId, Pageable pageable);
    Page<TempoPadraoProducao> findByEmpresaIdAndMaquinaId(Long empresaId, Long maquinaId, Pageable pageable);

    @Query("""
           select t from TempoPadraoProducao t
           where t.empresa.id = :empresaId and t.peca.id = :pecaId and t.setor.id = :setorId
             and t.ativo = true and t.vigenciaInicio <= :data
             and (t.vigenciaFim is null or t.vigenciaFim >= :data)
           order by case when t.maquina is null then 1 else 0 end, t.vigenciaInicio desc
           """)
    List<TempoPadraoProducao> findApplicable(@Param("empresaId") Long empresaId,
                                             @Param("pecaId") Long pecaId,
                                             @Param("setorId") Long setorId,
                                             @Param("data") LocalDate data);

    @Query("""
           select case when count(t) > 0 then true else false end from TempoPadraoProducao t
           where t.empresa.id = :empresaId and t.peca.id = :pecaId and t.setor.id = :setorId
             and ((:maquinaId is null and t.maquina is null) or t.maquina.id = :maquinaId)
             and t.ativo = true and (:id is null or t.id <> :id)
             and t.vigenciaInicio <= coalesce(:fim, t.vigenciaInicio)
             and (t.vigenciaFim is null or t.vigenciaFim >= :inicio)
           """)
    boolean existsConflict(@Param("empresaId") Long empresaId, @Param("pecaId") Long pecaId,
                           @Param("setorId") Long setorId, @Param("maquinaId") Long maquinaId,
                           @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("id") Long id);

    @Query("select t from TempoPadraoProducao t where t.empresa.id = :empresaId and t.ativo = true and t.vigenciaInicio <= :fim and (t.vigenciaFim is null or t.vigenciaFim >= :inicio)")
    @EntityGraph(attributePaths = {"peca", "setor", "maquina"})
    List<TempoPadraoProducao> findActiveForPeriod(@Param("empresaId") Long empresaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
