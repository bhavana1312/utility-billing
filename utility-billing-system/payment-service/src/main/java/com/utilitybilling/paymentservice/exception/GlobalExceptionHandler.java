package com.utilitybilling.paymentservice.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e){
        ErrorResponse r=new ErrorResponse();
        r.setMessage(e.getMessage());
        r.setStatus(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(r);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException e){
        ErrorResponse r=new ErrorResponse();
        r.setMessage(e.getMessage());
        r.setStatus(HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(r);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e){
        ErrorResponse r=new ErrorResponse();
        r.setMessage(e.getMessage());
        r.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e){
        ErrorResponse r=new ErrorResponse();
        r.setMessage("Internal server error");
        r.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
    }
    
    @ExceptionHandler(InvoicePdfGenerationException.class)
    public ResponseEntity<Map<String,Object>> handleInvoicePdfGenerationException(
            InvoicePdfGenerationException ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp",Instant.now(),
                        "status",HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "error","Invoice PDF Generation Failed",
                        "message",ex.getMessage()
                ));
    }
}
