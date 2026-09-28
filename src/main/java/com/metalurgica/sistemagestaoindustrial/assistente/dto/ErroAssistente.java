package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;

public record ErroAssistente(AssistenteErrorCode code, String message, String field) {
}
