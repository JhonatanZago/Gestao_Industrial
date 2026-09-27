package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndCodigoAndIdNot(Long empresaId, String codigo, Long id);
    Optional<Funcionario> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<Funcionario> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    Page<Funcionario> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<Funcionario> findByEmpresaIdAndAtivo(Long empresaId, Boolean ativo, Pageable pageable);
    Page<Funcionario> findByEmpresaIdAndCodigoContainingIgnoreCase(Long empresaId, String codigo, Pageable pageable);
    Page<Funcionario> findByEmpresaIdAndNomeContainingIgnoreCase(Long empresaId, String nome, Pageable pageable);
}
