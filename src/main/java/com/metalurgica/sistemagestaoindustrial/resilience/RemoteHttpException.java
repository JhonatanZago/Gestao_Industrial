package com.metalurgica.sistemagestaoindustrial.resilience;

public class RemoteHttpException extends RuntimeException {
    private final int status;
    public RemoteHttpException(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
}
