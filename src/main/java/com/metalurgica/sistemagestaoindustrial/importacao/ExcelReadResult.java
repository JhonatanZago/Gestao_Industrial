package com.metalurgica.sistemagestaoindustrial.importacao;
import java.util.*;
public record ExcelReadResult(List<LinhaProducaoImportada> linhas,List<ImportacaoLinhaErro> erros,int totalLinhas) { }
