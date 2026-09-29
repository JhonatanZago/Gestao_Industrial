package com.metalurgica.sistemagestaoindustrial.exception;

public class ImportProcessingException extends RuntimeException {
    public ImportProcessingException(Throwable cause) {
        super("Falha técnica ao processar importação.", cause);
    }
}
