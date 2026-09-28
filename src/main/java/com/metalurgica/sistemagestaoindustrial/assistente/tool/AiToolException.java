package com.metalurgica.sistemagestaoindustrial.assistente.tool;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;

public class AiToolException extends RuntimeException {
    private final AssistenteErrorCode code;

    public AiToolException(AssistenteErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public AssistenteErrorCode getCode() {
        return code;
    }
}
