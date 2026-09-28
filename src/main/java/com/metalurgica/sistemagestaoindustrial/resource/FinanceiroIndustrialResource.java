package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.analytics.*;
import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.service.FinanceiroIndustrialService;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController @RequestMapping("/api/v1/empresas/{empresaId}/analytics/financeiro")
public class FinanceiroIndustrialResource {
    private final FinanceiroIndustrialService service;
    public FinanceiroIndustrialResource(FinanceiroIndustrialService service) { this.service = service; }
    @GetMapping("/resumo") public List<ResumoFinanceiroIndustrialResponse> summary(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters f){return service.summary(empresaId,f.query(inicio,fim));}
    @GetMapping("/setores") public Page<ValorFinanceiroResponse> setores(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,Filters f,@PageableDefault(size=20) Pageable p){return service.grouped(empresaId,f.query(inicio,fim),Dimension.SETOR,p);}
    @GetMapping("/funcionarios") public Page<ValorFinanceiroResponse> funcionarios(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,Filters f,@PageableDefault(size=20) Pageable p){return service.grouped(empresaId,f.query(inicio,fim),Dimension.FUNCIONARIO,p);}
    @GetMapping("/pecas") public Page<ValorFinanceiroResponse> pecas(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,Filters f,@PageableDefault(size=20) Pageable p){return service.grouped(empresaId,f.query(inicio,fim),Dimension.PECA,p);}
    @GetMapping("/maquinas") public Page<ValorFinanceiroResponse> maquinas(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,Filters f,@PageableDefault(size=20) Pageable p){return service.grouped(empresaId,f.query(inicio,fim),Dimension.MAQUINA,p);}
    @GetMapping("/turnos") public Page<ValorFinanceiroResponse> turnos(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,Filters f,@PageableDefault(size=20) Pageable p){return service.grouped(empresaId,f.query(inicio,fim),Dimension.TURNO,p);}
    @GetMapping("/serie-temporal") public List<SerieFinanceiraResponse> series(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,@RequestParam GranularidadeAnalitica granularidade,Filters f){return service.series(empresaId,f.query(inicio,fim),granularidade);}
    @GetMapping("/comparativo") public List<ComparativoFinanceiroResponse> compare(@PathVariable Long empresaId,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicioAtual,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fimAtual,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicioAnterior,@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fimAnterior,Filters f){return service.compare(empresaId,f.query(inicioAtual,fimAtual),f.query(inicioAnterior,fimAnterior));}
    public record Filters(Long setorId, Long funcionarioId, Long pecaId, Long maquinaId, Long turnoId, String moeda, TipoValor tipoValor) { public FinanceiroQuery query(LocalDateTime inicio, LocalDateTime fim){return new FinanceiroQuery(inicio,fim,setorId,funcionarioId,pecaId,maquinaId,turnoId,moeda,tipoValor);} }
}
