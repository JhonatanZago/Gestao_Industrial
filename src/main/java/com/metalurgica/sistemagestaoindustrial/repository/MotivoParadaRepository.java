package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.MotivoParada;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MotivoParadaRepository extends JpaRepository<MotivoParada, Long> {
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndCodigoAndIdNot(Long empresaId, String codigo, Long id);
    Optional<MotivoParada> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<MotivoParada> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    Page<MotivoParada> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<MotivoParada> findByEmpresaIdAndAtivo(Long empresaId, Boolean ativo, Pageable pageable);
    Page<MotivoParada> findByEmpresaIdAndCodigoContainingIgnoreCase(Long empresaId, String codigo, Pageable pageable);
    Page<MotivoParada> findByEmpresaIdAndNomeContainingIgnoreCase(Long empresaId, String nome, Pageable pageable);
    Page<MotivoParada> findByEmpresaIdAndCategoriaContainingIgnoreCase(Long empresaId, String categoria, Pageable pageable);
}
