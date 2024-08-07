package com.docdoc.oauth2.exception;

import com.docdoc.oauth2.model.db.OAuthToken;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OAuthException extends Exception {

    private HttpStatus httpStatus;

    private OAuthException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public OAuthException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public static OAuthException userNotFound() {
        return new OAuthException("user not found", HttpStatus.NOT_FOUND);
    }
    public static OAuthException badRequest(){
        return new OAuthException("bad request", HttpStatus.BAD_REQUEST);
    }

}
