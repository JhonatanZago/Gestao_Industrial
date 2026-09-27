package com.metalurgica.sistemagestaoindustrial.resource;
import com.metalurgica.sistemagestaoindustrial.domain.StatusImportacao; import com.metalurgica.sistemagestaoindustrial.dto.response.*; import com.metalurgica.sistemagestaoindustrial.service.ImportacaoProducaoService; import org.springframework.data.domain.*; import org.springframework.data.web.PageableDefault; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile;
@RestController @RequestMapping("/api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao") public class ImportacaoProducaoResource {
 private final ImportacaoProducaoService service; public ImportacaoProducaoResource(ImportacaoProducaoService s){service=s;}
 @PostMapping("/validar") public ImportacaoProducaoResponse validate(@PathVariable Long empresaId,@PathVariable Long setorId,@RequestPart("file") MultipartFile file){return service.validate(empresaId,setorId,file);}
 @PostMapping public ResponseEntity<ImportacaoProducaoResponse> importFile(@PathVariable Long empresaId,@PathVariable Long setorId,@RequestPart("file") MultipartFile file){return ResponseEntity.status(HttpStatus.CREATED).body(service.importFile(empresaId,setorId,file));}
 @GetMapping public Page<ImportacaoProducaoResponse> list(@PathVariable Long empresaId,@PathVariable Long setorId,@RequestParam(required=false) StatusImportacao status,@PageableDefault(size=20) Pageable p){return service.list(empresaId,setorId,status,p);}
 @GetMapping("/{importacaoId}") public ImportacaoProducaoResponse get(@PathVariable Long empresaId,@PathVariable Long setorId,@PathVariable Long importacaoId){return service.get(empresaId,importacaoId);}
 @GetMapping("/{importacaoId}/linhas") public Page<ResultadoLinhaImportacaoResponse> lines(@PathVariable Long empresaId,@PathVariable Long setorId,@PathVariable Long importacaoId,@PageableDefault(size=50) Pageable p){return service.lines(empresaId,importacaoId,p);}
}
