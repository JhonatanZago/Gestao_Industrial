package com.metalurgica.sistemagestaoindustrial.observability;

import com.metalurgica.sistemagestaoindustrial.exception.ErrorCode;
import com.metalurgica.sistemagestaoindustrial.security.SecurityErrorWriter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class PayloadLimitFilter extends OncePerRequestFilter {
    private final OperationalProperties properties;
    private final SecurityErrorWriter errorWriter;

    public PayloadLimitFilter(OperationalProperties properties, SecurityErrorWriter errorWriter) {
        this.properties = properties;
        this.errorWriter = errorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        return !("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long max = properties.getMaxPayloadBytes();
        long declared = request.getContentLengthLong();
        if (declared > max) {
            reject(request, response);
            return;
        }
        String type = request.getContentType();
        boolean multipart = type != null && type.toLowerCase(Locale.ROOT).startsWith("multipart/");
        if (multipart || declared >= 0) {
            chain.doFilter(request, response);
            return;
        }
        byte[] body;
        try {
            body = readLimited(request.getInputStream(), max);
        } catch (PayloadTooLargeException exception) {
            reject(request, response);
            return;
        }
        chain.doFilter(new CachedBodyRequest(request, body), response);
    }

    static byte[] readLimited(InputStream input, long max) throws IOException {
        var output = new ByteArrayOutputStream((int) Math.min(max, 8192));
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > max) throw new PayloadTooLargeException();
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        errorWriter.write(request, response, HttpStatus.PAYLOAD_TOO_LARGE, ErrorCode.VALIDATION_ERROR,
                "Payload excede o limite permitido.");
    }

    private static final class PayloadTooLargeException extends IOException { }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;
        private CachedBodyRequest(HttpServletRequest request, byte[] body) { super(request); this.body = body; }
        @Override public int getContentLength() { return body.length; }
        @Override public long getContentLengthLong() { return body.length; }
        @Override public BufferedReader getReader() { return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding() == null ? java.nio.charset.StandardCharsets.UTF_8 : java.nio.charset.Charset.forName(getCharacterEncoding()))); }
        @Override public ServletInputStream getInputStream() {
            var input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public boolean isFinished() { return input.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) { }
                @Override public int read() { return input.read(); }
                @Override public int read(byte[] bytes, int offset, int length) { return input.read(bytes, offset, length); }
            };
        }
    }
}
