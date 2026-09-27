package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.Peca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PecaRepository extends JpaRepository<Peca, Long> {
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndCodigoAndIdNot(Long empresaId, String codigo, Long id);
    Optional<Peca> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<Peca> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    Page<Peca> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<Peca> findByEmpresaIdAndAtivo(Long empresaId, Boolean ativo, Pageable pageable);
    Page<Peca> findByEmpresaIdAndCodigoContainingIgnoreCase(Long empresaId, String codigo, Pageable pageable);
    Page<Peca> findByEmpresaIdAndDescricaoContainingIgnoreCase(Long empresaId, String descricao, Pageable pageable);
}
