package com.starline.resi.exceptions;

public class DataNotFoundException extends ApiException {

    public DataNotFoundException(String message) {
        super(message);
        this.httpCode = 404;
    }
}
