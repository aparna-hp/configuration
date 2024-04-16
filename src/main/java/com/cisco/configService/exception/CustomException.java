package com.cisco.configService.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatusCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class CustomException extends RuntimeException{

    private String errorDetails = null;

    private HttpStatusCode httpStatus = null;

    private String message = null;

    public CustomException() {
        super();
    }

    public CustomException(String message) {
        super();
        this.message = message;
    }

    public CustomException(HttpStatusCode httpStatus , String message) {
        super();
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public CustomException(HttpStatusCode httpStatus,String message,String errorDetails) {
        super();
        this.errorDetails = errorDetails;
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
