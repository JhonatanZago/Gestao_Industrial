package com.metalurgica.sistemagestaoindustrial.assistente.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;

@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private boolean enabled;
    private int maxQuestionLength = 2_000;
    private int maxToolCalls = 8;
    private int maxToolResults = 1_000;
    private Duration timeout = Duration.ofSeconds(30);
    private int maxOutputTokens = 1_000;
    private int maxRetrievedDocuments = 8;
    private long maxDocumentBytes = 5L * 1024 * 1024;
    private int maxRequestsPerMinute = 30;
    private int retryAttempts = 1;
    private String timezone = "America/Sao_Paulo";
    private boolean pdfEnabled;
    private int chunkSize = 1_200;
    private int chunkOverlap = 100;

    @PostConstruct
    public void validate() {
        if (maxQuestionLength < 1
                || maxToolCalls < 1
                || maxToolResults < 1
                || timeout == null
                || timeout.isZero()
                || timeout.isNegative()
                || maxOutputTokens < 1
                || maxRetrievedDocuments < 1
                || maxDocumentBytes < 1
                || maxRequestsPerMinute < 1
                || retryAttempts < 0
                || retryAttempts > 2
                || timezone == null
                || timezone.isBlank()
                || chunkSize < 100
                || chunkOverlap < 0
                || chunkOverlap >= chunkSize) {
            throw new IllegalStateException("Configura\u00e7\u00e3o de IA inv\u00e1lida.");
        }
        try {
            ZoneId.of(timezone);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("AI_TIMEZONE inv\u00e1lido.", exception);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxQuestionLength() {
        return maxQuestionLength;
    }

    public void setMaxQuestionLength(int maxQuestionLength) {
        this.maxQuestionLength = maxQuestionLength;
    }

    public int getMaxToolCalls() {
        return maxToolCalls;
    }

    public void setMaxToolCalls(int maxToolCalls) {
        this.maxToolCalls = maxToolCalls;
    }

    public int getMaxToolResults() {
        return maxToolResults;
    }

    public void setMaxToolResults(int maxToolResults) {
        this.maxToolResults = maxToolResults;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public int getMaxRetrievedDocuments() {
        return maxRetrievedDocuments;
    }

    public void setMaxRetrievedDocuments(int maxRetrievedDocuments) {
        this.maxRetrievedDocuments = maxRetrievedDocuments;
    }

    public long getMaxDocumentBytes() {
        return maxDocumentBytes;
    }

    public void setMaxDocumentBytes(long maxDocumentBytes) {
        this.maxDocumentBytes = maxDocumentBytes;
    }

    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isPdfEnabled() {
        return pdfEnabled;
    }

    public void setPdfEnabled(boolean pdfEnabled) {
        this.pdfEnabled = pdfEnabled;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }
}
