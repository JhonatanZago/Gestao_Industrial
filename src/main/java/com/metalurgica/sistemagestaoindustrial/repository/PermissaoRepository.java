package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    Optional<Permissao> findByCodigo(String codigo);
}
