package com.metalurgica.sistemagestaoindustrial.importacao;
import java.util.*;
public record ValidacaoImportacaoResult(List<LinhaValidadaImportacao> validas,List<LinhaValidadaImportacao> ignoradas,List<ImportacaoLinhaErro> erros,int totalLinhas) {
    public ValidacaoImportacaoResult(List<LinhaValidadaImportacao> validas,List<ImportacaoLinhaErro> erros,int totalLinhas) { this(validas, List.of(), erros, totalLinhas); }
    public boolean isValida(){return erros.isEmpty();}
}
