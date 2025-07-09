package com.starline.resi.exceptions;

public class TooManyActiveResiException extends ApiException {


    public TooManyActiveResiException() {
        super("Too many active resi");
        this.httpCode = 429;
    }
}
