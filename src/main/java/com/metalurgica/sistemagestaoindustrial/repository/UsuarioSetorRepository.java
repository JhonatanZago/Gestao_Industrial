package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.UsuarioSetor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface UsuarioSetorRepository extends JpaRepository<UsuarioSetor, Long> {
    @EntityGraph(attributePaths = "setor")
    List<UsuarioSetor> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndSetorId(Long usuarioId, Long setorId);

    void deleteByUsuarioIdAndSetorId(Long usuarioId, Long setorId);
}
