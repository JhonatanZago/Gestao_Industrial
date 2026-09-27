package com.metalurgica.sistemagestaoindustrial.repository;
import com.metalurgica.sistemagestaoindustrial.domain.Maquina;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndCodigoAndIdNot(Long empresaId, String codigo, Long id);
    Optional<Maquina> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<Maquina> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    java.util.List<Maquina> findByEmpresaIdAndCodigoIn(Long empresaId, java.util.Collection<String> codigos);
    Page<Maquina> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<Maquina> findByEmpresaIdAndSetorId(Long empresaId, Long setorId, Pageable pageable);
    Page<Maquina> findByEmpresaIdAndAtivo(Long empresaId, Boolean ativo, Pageable pageable);
    Page<Maquina> findByEmpresaIdAndCodigoContainingIgnoreCase(Long empresaId, String codigo, Pageable pageable);
    Page<Maquina> findByEmpresaIdAndNomeContainingIgnoreCase(Long empresaId, String nome, Pageable pageable);
    Page<Maquina> findByEmpresaIdAndTipoContainingIgnoreCase(Long empresaId, String tipo, Pageable pageable);
}
