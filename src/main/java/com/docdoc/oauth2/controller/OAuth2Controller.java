package com.docdoc.oauth2.controller;

import com.docdoc.oauth2.exception.OAuthException;
import com.docdoc.oauth2.model.dto.AuthDataDTO;
import com.docdoc.oauth2.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/oAuth2")
public class OAuth2Controller {
    private OAuth2Service oAuth2Service;

    @Autowired
    public OAuth2Controller(OAuth2Service oAuth2Service) {
        this.oAuth2Service = oAuth2Service;
    }

    @PostMapping
    public ResponseEntity<String> getOAuth2(@RequestBody AuthDataDTO authDataDTO) {
        Optional.of(authDataDTO).filter(data -> data.login() != null)
                .filter(data -> data.password() != null)
                .filter(data -> data.role()!= null)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        try {
            return new ResponseEntity(oAuth2Service.getAuthorize(authDataDTO),HttpStatus.OK);
        } catch(OAuthException exception){
            throw new ResponseStatusException(exception.getHttpStatus(), exception.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<Void> validateToken (@RequestParam String token) {
        Optional.of(token).filter(data -> !data.isEmpty())
                .filter(data -> !data.isBlank())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        try {
            oAuth2Service.validateToken(token);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(OAuthException exception){
            throw new ResponseStatusException(exception.getHttpStatus(), exception.getMessage());
        }

    }

}
