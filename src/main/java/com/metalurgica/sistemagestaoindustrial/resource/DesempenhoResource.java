package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.analytics.*;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.service.DesempenhoService;
import com.metalurgica.sistemagestaoindustrial.service.MetaProducaoService;
import com.metalurgica.sistemagestaoindustrial.domain.TipoMeta;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController @RequestMapping("/api/v1/empresas/{empresaId}/analytics")
public class DesempenhoResource {
    private final DesempenhoService service; private final MetaProducaoService metaService;
    public DesempenhoResource(DesempenhoService service, MetaProducaoService metaService){this.service=service;this.metaService=metaService;}
    @GetMapping("/metas") public Page<MetaProducaoResponse> metas(@PathVariable Long empresaId,@RequestParam(required=false) TipoMeta tipo,@RequestParam(required=false) Boolean ativo,@PageableDefault(size=20) Pageable p){return metaService.list(empresaId,tipo,ativo,p);}
    @GetMapping("/desempenho") public DesempenhoResponse summary(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, AnalyticsResource.Filters f){return service.summary(empresaId,f.query(inicio,fim));}
    @GetMapping("/desempenho/funcionarios") public Page<DesempenhoComparativoResponse> funcionarios(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, AnalyticsResource.Filters f,@PageableDefault(size=20) Pageable p){return service.compare(empresaId,f.query(inicio,fim),Dimension.FUNCIONARIO,p);}
    @GetMapping("/desempenho/setores") public Page<DesempenhoComparativoResponse> setores(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, AnalyticsResource.Filters f,@PageableDefault(size=20) Pageable p){return service.compare(empresaId,f.query(inicio,fim),Dimension.SETOR,p);}
    @GetMapping("/desempenho/maquinas") public Page<DesempenhoComparativoResponse> maquinas(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, AnalyticsResource.Filters f,@PageableDefault(size=20) Pageable p){return service.compare(empresaId,f.query(inicio,fim),Dimension.MAQUINA,p);}
    @GetMapping("/desempenho/pecas") public Page<DesempenhoComparativoResponse> pecas(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, AnalyticsResource.Filters f,@PageableDefault(size=20) Pageable p){return service.compare(empresaId,f.query(inicio,fim),Dimension.PECA,p);}
}
