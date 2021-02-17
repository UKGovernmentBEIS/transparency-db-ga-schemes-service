package com.beis.subsidy.ga.schemes.dbpublishingservice.exception;

import org.springframework.http.HttpStatus;

public class AccessTokenException extends RuntimeException{
    private final HttpStatus httpStatus;

    private final String errorMessage;

    public AccessTokenException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
