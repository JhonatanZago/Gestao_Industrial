package com.metalurgica.sistemagestaoindustrial.importacao;

import com.metalurgica.sistemagestaoindustrial.domain.ImportacaoBaseline;
import com.metalurgica.sistemagestaoindustrial.repository.ImportacaoBaselineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;

@Service
public class BootstrapBaselineService {
    private final ImportacaoBaselineRepository repository;

    public BootstrapBaselineService(ImportacaoBaselineRepository repository) { this.repository = repository; }

    @Transactional
    public Decision evaluate(Path file, AutomaticImportProperties.Directory source,
                             AutomaticImportProperties.InitialMode mode) {
        if (mode == AutomaticImportProperties.InitialMode.PROCESS_EXISTING) return Decision.PROCESS;
        String hash = hash(file);
        String sourceKey = hashText(file.toAbsolutePath().normalize().toString());
        String name = file.getFileName().toString();
        var existing = repository.findByEmpresaIdAndSetorIdAndSourceKeyAndNomeArquivo(
                source.getEmpresaId(), source.getSetorId(), sourceKey, name);
        if (existing.isEmpty()) {
            if (mode == AutomaticImportProperties.InitialMode.IGNORE_EXISTING) {
                save(source, sourceKey, name, hash);
                return Decision.SKIP_BASELINE;
            }
            return Decision.PROCESS;
        }
        return existing.get().getHashArquivo().equals(hash) ? Decision.SKIP_UNCHANGED : Decision.PROCESS;
    }

    @Transactional
    public void markProcessed(Path file, AutomaticImportProperties.Directory source) {
        String sourceKey = hashText(file.toAbsolutePath().normalize().toString());
        String name = file.getFileName().toString();
        String hash = hash(file);
        var item = repository.findByEmpresaIdAndSetorIdAndSourceKeyAndNomeArquivo(
                source.getEmpresaId(), source.getSetorId(), sourceKey, name).orElseGet(ImportacaoBaseline::new);
        item.setEmpresaId(source.getEmpresaId()); item.setSetorId(source.getSetorId());
        item.setSourceKey(sourceKey); item.setNomeArquivo(name); item.setHashArquivo(hash);
        item.setObservadoEm(OffsetDateTime.now()); repository.save(item);
    }

    private void save(AutomaticImportProperties.Directory source, String key, String name, String hash) {
        var item = new ImportacaoBaseline(); item.setEmpresaId(source.getEmpresaId()); item.setSetorId(source.getSetorId());
        item.setSourceKey(key); item.setNomeArquivo(name); item.setHashArquivo(hash); item.setObservadoEm(OffsetDateTime.now());
        repository.save(item);
    }

    private static String hash(Path file) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); } catch (Exception e) { throw new IllegalStateException("HASH_BASELINE_FAILURE", e); } }
    private static String hashText(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
    public enum Decision { PROCESS, SKIP_BASELINE, SKIP_UNCHANGED }
}
