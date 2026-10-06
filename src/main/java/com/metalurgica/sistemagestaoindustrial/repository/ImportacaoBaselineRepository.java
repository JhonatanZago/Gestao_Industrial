package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.domain.ImportacaoBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImportacaoBaselineRepository extends JpaRepository<ImportacaoBaseline, Long> {
    Optional<ImportacaoBaseline> findByEmpresaIdAndSetorIdAndSourceKeyAndNomeArquivo(
            Long empresaId, Long setorId, String sourceKey, String nomeArquivo);
}
