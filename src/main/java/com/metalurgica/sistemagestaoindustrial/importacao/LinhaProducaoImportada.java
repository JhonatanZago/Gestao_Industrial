package com.metalurgica.sistemagestaoindustrial.importacao;
import java.math.BigDecimal; import java.time.*;
public record LinhaProducaoImportada(int linha,String codigoFuncionario,String codigoPeca,BigDecimal quantidade,LocalDate data,LocalTime inicio,LocalTime fim,String setor,String codigoMaquina,String codigoTurno) { public LocalDateTime inicioDateTime(){return data==null||inicio==null?null:LocalDateTime.of(data,inicio);} public LocalDateTime fimDateTime(boolean overnight){return data==null||fim==null?null:LocalDateTime.of(data.plusDays(overnight?1:0),fim);} }
