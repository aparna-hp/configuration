package com.cisco.configService.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@ControllerAdvice
@Slf4j
class ExceptionHandlingAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    ResponseEntity<VoilationResponse> onConstraintValidationException(
            ConstraintViolationException e) {
        VoilationResponse error = new VoilationResponse();
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            log.info("Field " + violation.getPropertyPath().toString() + " Message " + violation.getMessage());
            error.getViolations().add(
                    new Violation(violation.getPropertyPath().toString(), violation.getMessage()));
        }
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    ResponseEntity<VoilationResponse> onMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        VoilationResponse error = new VoilationResponse();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            log.info("Field " + fieldError.getField() + " Message " + fieldError.getDefaultMessage());
            error.getViolations().add(
                    new Violation(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(HttpMessageNotReadableException e) {
        log.info("On Json Parsing exception from controller:" + e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorDetails(e.getMessage());
        errorResponse.setMessage("Invalid Json input!");
        errorResponse.setTimestamp(new Date());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    ResponseEntity<ErrorResponse> onCustomException(CustomException e) {
        log.info("On Custom Exception. Message: " + e.getMessage()
                + " Error details: " + e.getErrorDetails());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorDetails(e.getErrorDetails());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(new Date());

        HttpStatusCode httpStatus = (null == e.getHttpStatus()) ? HttpStatus.INTERNAL_SERVER_ERROR : e.getHttpStatus();

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    ResponseEntity<ErrorResponse> onException(Exception e) {
        log.info("Start Default handler for all exceptions");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500

        log.error(" Exception Stacktrace: " ,e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Error performing the operation.");
        errorResponse.setTimestamp(new Date());
        errorResponse.setErrorDetails(e.getMessage());

        return new ResponseEntity<>(errorResponse, status);
    }

}
