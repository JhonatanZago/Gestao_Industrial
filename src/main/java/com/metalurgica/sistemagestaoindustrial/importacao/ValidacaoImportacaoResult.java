package com.metalurgica.sistemagestaoindustrial.importacao;
import java.util.*;
public record ValidacaoImportacaoResult(List<LinhaValidadaImportacao> validas,List<ImportacaoLinhaErro> erros,int totalLinhas) { public boolean isValida(){return erros.isEmpty();} }
