// com.vishal.employeesystem.exception.ErrorResponse
package com.vishal.employeesystem.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private Map<String, String> errors;

    private ErrorResponse() {}

    // For single message errors → 404, 500
    public static ErrorResponse of(int status, String message) {
        ErrorResponse r = new ErrorResponse();
        r.timestamp = LocalDateTime.now();
        r.status    = status;
        r.message   = message;
        return r;
    }

    // For field-level validation errors → 400
    public static ErrorResponse ofValidation(int status, Map<String, String> errors) {
        ErrorResponse r = new ErrorResponse();
        r.timestamp = LocalDateTime.now();
        r.status    = status;
        r.errors    = errors;
        return r;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus()              { return status; }
    public String getMessage()          { return message; }
    public Map<String, String> getErrors() { return errors; }
}