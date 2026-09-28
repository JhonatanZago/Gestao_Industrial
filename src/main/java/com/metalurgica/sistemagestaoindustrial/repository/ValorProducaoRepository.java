package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import com.metalurgica.sistemagestaoindustrial.domain.ValorProducao;
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

public interface ValorProducaoRepository extends JpaRepository<ValorProducao, Long>, JpaSpecificationExecutor<ValorProducao> {
    Optional<ValorProducao> findByIdAndEmpresaId(Long id, Long empresaId);
    Page<ValorProducao> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<ValorProducao> findByEmpresaIdAndAtivo(Long empresaId, boolean ativo, Pageable pageable);
    Page<ValorProducao> findByEmpresaIdAndPecaId(Long empresaId, Long pecaId, Pageable pageable);
    Page<ValorProducao> findByEmpresaIdAndSetorId(Long empresaId, Long setorId, Pageable pageable);
    Page<ValorProducao> findByEmpresaIdAndMaquinaId(Long empresaId, Long maquinaId, Pageable pageable);
    Page<ValorProducao> findByEmpresaIdAndTipoValor(Long empresaId, TipoValor tipo, Pageable pageable);
    Page<ValorProducao> findByEmpresaIdAndMoeda(Long empresaId, String moeda, Pageable pageable);

    @Query("select v from ValorProducao v where v.empresa.id = :empresaId and v.ativo = true and v.vigenciaInicio <= :fim and (v.vigenciaFim is null or v.vigenciaFim >= :inicio)")
    @EntityGraph(attributePaths = {"peca", "setor", "maquina"})
    List<ValorProducao> findActiveForPeriod(@Param("empresaId") Long empresaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
           select case when count(v) > 0 then true else false end from ValorProducao v
           where v.empresa.id = :empresaId and v.peca.id = :pecaId and v.setor.id = :setorId
             and ((:maquinaId is null and v.maquina is null) or v.maquina.id = :maquinaId)
             and v.tipoValor = :tipoValor and v.moeda = :moeda and v.ativo = true
             and (:id is null or v.id <> :id)
             and v.vigenciaInicio <= coalesce(:fim, v.vigenciaInicio)
             and (v.vigenciaFim is null or v.vigenciaFim >= :inicio)
           """)
    boolean existsConflict(@Param("empresaId") Long empresaId, @Param("pecaId") Long pecaId, @Param("setorId") Long setorId,
                           @Param("maquinaId") Long maquinaId, @Param("tipoValor") TipoValor tipoValor, @Param("moeda") String moeda,
                           @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("id") Long id);
}
