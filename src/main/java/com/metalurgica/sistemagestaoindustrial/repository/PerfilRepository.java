package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.Perfil;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    @EntityGraph(attributePaths = "permissoes")
    Optional<Perfil> findByCodigo(String codigo);
}
