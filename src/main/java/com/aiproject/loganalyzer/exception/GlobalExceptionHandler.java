package com.aiproject.loganalyzer.exception;

import com.aiproject.loganalyzer.dto.LogResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<LogResponse> handleWebClientException(WebClientResponseException exception) {
        LogResponse response = new LogResponse();
        response.setAnomalySummary("External AI API Failure");
        response.setRootCause(List.of(exception.getMessage()));
        response.setRecommendation(List.of("Check API quota, key, or model configuration"));
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LogResponse> handleValidationException(MethodArgumentNotValidException exception) {
        LogResponse response = new LogResponse();
        String errorMessage = exception.getBindingResult().getFieldErrors().stream().findFirst().map(org.springframework.validation.FieldError::getDefaultMessage).orElse("Invalid request");
        response.setAnomalySummary("Validation Failed");
        response.setRootCause(List.of(errorMessage));
        response.setRecommendation(List.of("Provide valid request input"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<LogResponse> handleGenericException(Exception exception) {
        LogResponse response = new LogResponse();
        response.setAnomalySummary("Internal Server Error");
        response.setRootCause(List.of(exception.getMessage()));
        response.setRecommendation(List.of("Review application logs"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
