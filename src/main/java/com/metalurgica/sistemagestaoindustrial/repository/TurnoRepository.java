package com.metalurgica.sistemagestaoindustrial.repository;
import com.metalurgica.sistemagestaoindustrial.domain.Turno;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndCodigoAndIdNot(Long empresaId, String codigo, Long id);
    Optional<Turno> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<Turno> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    java.util.List<Turno> findByEmpresaIdAndCodigoIn(Long empresaId, java.util.Collection<String> codigos);
    Page<Turno> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<Turno> findByEmpresaIdAndAtivo(Long empresaId, Boolean ativo, Pageable pageable);
    Page<Turno> findByEmpresaIdAndCodigoContainingIgnoreCase(Long empresaId, String codigo, Pageable pageable);
    Page<Turno> findByEmpresaIdAndNomeContainingIgnoreCase(Long empresaId, String nome, Pageable pageable);
}
