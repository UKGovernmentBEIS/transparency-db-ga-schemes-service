package com.beis.subsidy.ga.schemes.dbpublishingservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AccessManagementException extends RuntimeException{
    private final HttpStatus httpStatus;

    private final String errorMessage;

    public AccessManagementException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
 }
