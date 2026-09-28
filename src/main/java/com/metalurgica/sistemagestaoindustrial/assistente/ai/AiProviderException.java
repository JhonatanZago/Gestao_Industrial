package com.metalurgica.sistemagestaoindustrial.assistente.ai;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;

public class AiProviderException extends RuntimeException {
    private final AssistenteErrorCode code;
    private final boolean transientFailure;

    public AiProviderException(AssistenteErrorCode code, String message, boolean transientFailure) {
        super(message);
        this.code = code;
        this.transientFailure = transientFailure;
    }

    public AssistenteErrorCode getCode() {
        return code;
    }

    public boolean isTransientFailure() {
        return transientFailure;
    }
}
