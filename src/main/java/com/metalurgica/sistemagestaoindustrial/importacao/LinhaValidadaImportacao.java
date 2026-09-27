package com.metalurgica.sistemagestaoindustrial.importacao;
import com.metalurgica.sistemagestaoindustrial.domain.*; import java.time.*; import java.math.BigDecimal;
public record LinhaValidadaImportacao(LinhaProducaoImportada linha,Funcionario funcionario,Peca peca,Maquina maquina,Turno turno,LocalDateTime inicio,LocalDateTime fim) { public BigDecimal quantidade(){return linha.quantidade();} }
