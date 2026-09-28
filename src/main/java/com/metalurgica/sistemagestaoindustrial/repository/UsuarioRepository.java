package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.StatusUsuario;
import com.metalurgica.sistemagestaoindustrial.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @EntityGraph(attributePaths = {"empresa", "perfil", "perfil.permissoes"})
    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = {"empresa", "perfil", "perfil.permissoes"})
    Optional<Usuario> findById(Long id);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"empresa", "perfil"})
    Page<Usuario> findByEmpresaIdAndStatus(Long empresaId, StatusUsuario status, Pageable pageable);

    @EntityGraph(attributePaths = {"empresa", "perfil"})
    Page<Usuario> findByEmpresaId(Long empresaId, Pageable pageable);
}
