package com.metalurgica.sistemagestaoindustrial.importacao;

import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class AutomaticImportConfigurationValidator {
    public List<String> validate(AutomaticImportProperties properties) {
        var errors = new ArrayList<String>();
        for (var source : properties.getDirectories()) {
            if (source.getEmpresaId() == null || source.getSetorId() == null) errors.add("empresa/setor ausente");
            if (source.getPath() == null || source.getPath().isBlank()) { errors.add("path ausente"); continue; }
            try {
                var path = Path.of(source.getPath()).toAbsolutePath().normalize();
                if (!Files.exists(path) || !Files.isReadable(path)) errors.add("path inacessível");
                else if (source.getKind() == AutomaticImportProperties.SourceKind.DIRECTORY && !Files.isDirectory(path)) errors.add("path não é diretório");
                else if (source.getKind() == AutomaticImportProperties.SourceKind.FILE && (!Files.isRegularFile(path) || !AutomaticImportFilePolicy.eligible(path, source))) errors.add("arquivo específico não suportado");
            } catch (RuntimeException e) { errors.add("path inválido"); }
        }
        return errors;
    }
}
