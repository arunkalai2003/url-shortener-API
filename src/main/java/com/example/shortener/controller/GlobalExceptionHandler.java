package com.example.shortener.controller;

import com.example.shortener.dto.ApiError;
import com.example.shortener.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiError> bad(BadRequestException e, HttpServletRequest r){ return error(HttpStatus.BAD_REQUEST,"BAD_REQUEST",e.getMessage(),r); }
    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException e, HttpServletRequest r){ return error(HttpStatus.CONFLICT,"CONFLICT",e.getMessage(),r); }
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e, HttpServletRequest r){ return error(HttpStatus.NOT_FOUND,"NOT_FOUND",e.getMessage(),r); }
    @ExceptionHandler(GoneException.class)
    ResponseEntity<ApiError> gone(GoneException e, HttpServletRequest r){ return error(HttpStatus.GONE,"GONE",e.getMessage(),r); }
    @ExceptionHandler(ServiceUnavailableException.class)
    ResponseEntity<ApiError> unavailable(ServiceUnavailableException e, HttpServletRequest r){ return error(HttpStatus.SERVICE_UNAVAILABLE,"DEPENDENCY_UNAVAILABLE",e.getMessage(),r); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest r){
        String msg=e.getBindingResult().getFieldErrors().stream().findFirst().map(f->f.getField()+": "+f.getDefaultMessage()).orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR",msg,r);
    }
    private ResponseEntity<ApiError> error(HttpStatus s,String code,String m,HttpServletRequest r){
        String cid=(String)r.getAttribute("correlationId"); if(cid==null) cid=UUID.randomUUID().toString();
        return ResponseEntity.status(s).body(new ApiError(code,m,cid,Instant.now()));
    }
}
