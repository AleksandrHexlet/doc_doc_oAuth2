package com.docdoc.oauth2.service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;

import com.docdoc.oauth2.configuration.RoleType;
import com.docdoc.oauth2.exception.OAuthException;
import com.docdoc.oauth2.model.db.OAuthToken;
import com.docdoc.oauth2.model.dto.AuthDataDTO;
import com.docdoc.oauth2.openfeign.UserRequest;
import com.docdoc.oauth2.repository.OAuthRepository;

@Service
public class OAuth2Service {
    private final UserRequest userRequest;
    private final JWTHandlerService jwtHandlerService;
    private final OAuthRepository oAuthRepository;
    @Value("${ru.docdoc.oauth.life.span.token}")
    private int lifetimeJWTtoken;

    public OAuth2Service(UserRequest userRequest, JWTHandlerService jwtHandlerService, OAuthRepository oAuthRepository) {
        this.userRequest = userRequest;
        this.jwtHandlerService = jwtHandlerService;
        this.oAuthRepository = oAuthRepository;
    }

    public String getAuthorize(AuthDataDTO authDataDTO) throws OAuthException {
//        if (authDataDTO.login() == null || authDataDTO.password() == null || authDataDTO.role() == null) {
//            throw OAuthException.userNotFound();
//        }
        LocalDateTime date = LocalDateTime.now();

        return Optional.of(userRequest.getOAuth2(authDataDTO))
                .filter(response -> response.getStatusCode() != HttpStatus.NOT_FOUND && response.getBody() != null)
                .map(response -> {
                    try {
                        Iterable<OAuthToken> users = oAuthRepository
                                .findByLoginAndExpiredAtGreaterThanAndEnableTrue(response.getBody().getLogin(),
                                        LocalDateTime.now()).stream().peek(user -> user.setEnable(false))
                                .toList();
                        if (users.iterator().hasNext()) {
                            oAuthRepository.saveAll(users);
                        }
                        String token = jwtHandlerService.generateToken(response.getBody());
                        OAuthToken tokenNew = OAuthToken.builder().token(token).login(response.getBody().getLogin()).role(RoleType.USER)
                                .createdAt(date).expiredAt(date.plusDays(lifetimeJWTtoken)).enable(true)
                                .userId(response.getBody().getUserId())
                                .build();
                        return oAuthRepository.save(tokenNew).getToken();
                    } catch (JOSEException e) { // TODO Exception сделай в lambda; cheked and unchecked exception
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(OAuthException::userNotFound);
    }

    public void validateToken(String token) throws OAuthException {
        try {
            Optional<OAuthToken> oAuthToken = oAuthRepository.findByToken(token);
            if (oAuthToken.isEmpty() || !jwtHandlerService
                    .isTokenValid(token, oAuthToken.get().getLogin(),
                            oAuthToken.get().getRole())) {
                throw OAuthException.badRequest();
            }
        } catch (BadJOSEException | ParseException | JOSEException e) {
            throw OAuthException.badRequest();
        }
    }
}
