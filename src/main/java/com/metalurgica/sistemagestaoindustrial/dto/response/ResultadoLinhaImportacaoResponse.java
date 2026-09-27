package com.metalurgica.sistemagestaoindustrial.dto.response;
public record ResultadoLinhaImportacaoResponse(Long id,int numeroLinha,String status,String codigoFuncionario,String codigoPeca,String codigoErro,String mensagem) { }
