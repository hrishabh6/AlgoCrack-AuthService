package com.hrishabh.algocrack.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class StructuredLogger {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger slf4jLogger;
    private final String serviceName;
    private final String component;

    public StructuredLogger(Class<?> clazz, String serviceName) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
        this.serviceName = serviceName;
        this.component = clazz.getSimpleName();
    }

    public StructuredLogger(Class<?> clazz, String serviceName, String component) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
        this.serviceName = serviceName;
        this.component = component;
    }

    public void info(String message, Object... fields) {
        logAtLevel("INFO", message, fields);
    }

    public void warn(String message, Object... fields) {
        logAtLevel("WARN", message, fields);
    }

    public void error(String message, Object... fields) {
        logAtLevel("ERROR", message, fields);
    }

    public void error(String message, Throwable throwable, Object... fields) {
        Map<String, Object> logEntry = buildLogEntry("ERROR", message, fields);
        if (throwable != null) {
            logEntry.put(LoggingConstants.EXCEPTION_CLASS, throwable.getClass().getName());
            logEntry.put(LoggingConstants.ERROR_MESSAGE, throwable.getMessage());
            logEntry.put(LoggingConstants.STACK_TRACE, getStackTrace(throwable));
        }
        slf4jLogger.error(serializeToJson(logEntry));
    }

    public void debug(String message, Object... fields) {
        if (slf4jLogger.isDebugEnabled()) {
            logAtLevel("DEBUG", message, fields);
        }
    }

    public void logRequest(String requestId, String method, String path, String userAgent, String remoteIp,
            Object... additionalFields) {
        Map<String, Object> logEntry = buildLogEntry("INFO", "Incoming HTTP request", additionalFields);
        logEntry.put(LoggingConstants.REQUEST_ID, requestId);
        logEntry.put(LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.REQUEST);
        logEntry.put(LoggingConstants.HTTP_METHOD, method);
        logEntry.put(LoggingConstants.HTTP_PATH, path);
        logEntry.put(LoggingConstants.USER_AGENT, userAgent);
        logEntry.put(LoggingConstants.REMOTE_IP, remoteIp);
        slf4jLogger.info(serializeToJson(logEntry));
    }

    public void logResponse(String requestId, int statusCode, long durationMs, Object... additionalFields) {
        Map<String, Object> logEntry = buildLogEntry("INFO", "HTTP response", additionalFields);
        logEntry.put(LoggingConstants.REQUEST_ID, requestId);
        logEntry.put(LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.RESPONSE);
        logEntry.put(LoggingConstants.HTTP_STATUS, statusCode);
        logEntry.put(LoggingConstants.TYPE, LoggingConstants.getHttpStatusType(statusCode));
        logEntry.put(LoggingConstants.DURATION_MS, durationMs);
        slf4jLogger.info(serializeToJson(logEntry));
    }

    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    private void logAtLevel(String level, String message, Object... fields) {
        Map<String, Object> logEntry = buildLogEntry(level, message, fields);
        String jsonLog = serializeToJson(logEntry);

        switch (level) {
            case "ERROR":
                slf4jLogger.error(jsonLog);
                break;
            case "WARN":
                slf4jLogger.warn(jsonLog);
                break;
            case "DEBUG":
                slf4jLogger.debug(jsonLog);
                break;
            case "INFO":
            default:
                slf4jLogger.info(jsonLog);
                break;
        }
    }

    private Map<String, Object> buildLogEntry(String level, String message, Object... fields) {
        Map<String, Object> logEntry = new LinkedHashMap<>();

        logEntry.put(LoggingConstants.TIMESTAMP, System.currentTimeMillis());
        logEntry.put(LoggingConstants.LEVEL, level);
        logEntry.put(LoggingConstants.MESSAGE, message);
        logEntry.put(LoggingConstants.SERVICE, serviceName);
        logEntry.put(LoggingConstants.KUBE_MICRO, serviceName);
        logEntry.put(LoggingConstants.COMPONENT, component);

        String requestId = RequestContext.getRequestId();
        if (requestId != null) {
            logEntry.put(LoggingConstants.REQUEST_ID, requestId);
        }

        if (fields != null && fields.length > 0) {
            if (fields.length % 2 != 0) {
                throw new IllegalArgumentException("Fields must be passed as key-value pairs");
            }
            for (int i = 0; i < fields.length; i += 2) {
                String key = String.valueOf(fields[i]);
                Object value = fields[i + 1];
                logEntry.put(key, value);
            }
        }

        return logEntry;
    }

    private String serializeToJson(Map<String, Object> logEntry) {
        try {
            return objectMapper.writeValueAsString(logEntry);
        } catch (Exception e) {
            return logEntry.toString();
        }
    }

    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element).append("\n");
        }
        return sb.toString();
    }
}
