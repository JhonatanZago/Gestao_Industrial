package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndNomeNormalizado(Long empresaId, String nomeNormalizado);
    boolean existsByEmpresaIdAndCodigoAndIdNot(Long empresaId, String codigo, Long id);
    boolean existsByEmpresaIdAndNomeNormalizadoAndIdNot(Long empresaId, String nomeNormalizado, Long id);
    Optional<Setor> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<Setor> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    Page<Setor> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<Setor> findByEmpresaIdAndAtivo(Long empresaId, Boolean ativo, Pageable pageable);
    Page<Setor> findByEmpresaIdAndNomeContainingIgnoreCase(Long empresaId, String nome, Pageable pageable);
    Page<Setor> findByEmpresaIdAndCodigoContainingIgnoreCase(Long empresaId, String codigo, Pageable pageable);
}
