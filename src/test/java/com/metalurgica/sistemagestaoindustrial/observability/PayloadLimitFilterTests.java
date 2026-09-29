package com.metalurgica.sistemagestaoindustrial.observability;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import com.metalurgica.sistemagestaoindustrial.exception.ErrorCode;
import com.metalurgica.sistemagestaoindustrial.security.SecurityErrorWriter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class PayloadLimitFilterTests {
    @Test
    void readsUnknownLengthOnlyUpToConfiguredLimit() throws Exception {
        assertArrayEquals(new byte[]{1, 2, 3},
                PayloadLimitFilter.readLimited(new ByteArrayInputStream(new byte[]{1, 2, 3}), 3));
        assertThrows(IOException.class, () -> PayloadLimitFilter.readLimited(
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4}), 3));
    }

    @Test
    void rejectsKnownMultipartLengthBeforeTheChain() throws Exception {
        var properties = new OperationalProperties(); properties.setMaxPayloadBytes(32);
        var writer = Mockito.mock(SecurityErrorWriter.class);
        var filter = new PayloadLimitFilter(properties, writer);
        var request = new MockHttpServletRequest(); request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=test"); request.setContent(new byte[64]);
        var response = new MockHttpServletResponse();
        var chain = Mockito.mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verifyNoInteractions(chain);
        Mockito.verify(writer).write(Mockito.eq(request), Mockito.eq(response),
                Mockito.eq(HttpStatus.PAYLOAD_TOO_LARGE), Mockito.eq(ErrorCode.VALIDATION_ERROR), Mockito.anyString());
    }
}
