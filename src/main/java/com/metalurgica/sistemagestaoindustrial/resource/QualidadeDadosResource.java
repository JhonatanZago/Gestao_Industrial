package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.response.DuplicidadeProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ProblemaQualidadeResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.QualidadeDadosResumoResponse;
import com.metalurgica.sistemagestaoindustrial.quality.*;
import com.metalurgica.sistemagestaoindustrial.service.QualidadeDadosService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/qualidade-dados")
public class QualidadeDadosResource {
    private final QualidadeDadosService service;

    public QualidadeDadosResource(QualidadeDadosService service) { this.service = service; }

    @GetMapping("/resumo")
    public QualidadeDadosResumoResponse resumo(@PathVariable Long empresaId, QueryParameters parameters) {
        return service.resumo(empresaId, parameters.query());
    }

    @GetMapping("/problemas")
    public Page<ProblemaQualidadeResponse> problemas(@PathVariable Long empresaId, QueryParameters parameters,
                                                      @PageableDefault(size = 50) Pageable pageable) {
        return service.problemas(empresaId, parameters.query(), pageable);
    }

    @GetMapping("/duplicidades")
    public Page<DuplicidadeProducaoResponse> duplicidades(@PathVariable Long empresaId, QueryParameters parameters,
                                                          @PageableDefault(size = 50) Pageable pageable) {
        return service.duplicidades(empresaId, parameters.query(), pageable);
    }

    @GetMapping("/recursos/{tipoRecurso}/{recursoId}")
    public List<ProblemaQualidadeResponse> recurso(@PathVariable Long empresaId,
                                                    @PathVariable TipoRecursoQualidadeDados tipoRecurso,
                                                    @PathVariable Long recursoId,
                                                    QueryParameters parameters) {
        return service.problemasDoRecurso(empresaId, tipoRecurso, recursoId, parameters.query());
    }

    public record QueryParameters(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam(required = false) Long pecaId,
            @RequestParam(required = false) Long maquinaId,
            @RequestParam(required = false) Long turnoId,
            @RequestParam(required = false) CategoriaQualidadeDados categoria,
            @RequestParam(required = false) SeveridadeQualidadeDados severidade,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) TipoRecursoQualidadeDados tipoRecurso,
            @RequestParam(required = false) Long recursoId) {
        public QualidadeDadosQuery query() {
            return new QualidadeDadosQuery(inicio, fim, setorId, funcionarioId, pecaId, maquinaId, turnoId, categoria, severidade, codigo, tipoRecurso, recursoId);
        }
    }
}
