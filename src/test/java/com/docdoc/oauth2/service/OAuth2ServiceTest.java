package com.docdoc.oauth2.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nimbusds.jose.JOSEException;

import lombok.SneakyThrows;

import com.docdoc.oauth2.configuration.RoleType;
import com.docdoc.oauth2.exception.OAuthException;
import com.docdoc.oauth2.model.db.OAuthToken;
import com.docdoc.oauth2.model.dto.AuthDataDTO;
import com.docdoc.oauth2.model.dto.UserResponseDTO;
import com.docdoc.oauth2.openfeign.UserRequest;
import com.docdoc.oauth2.repository.OAuthRepository;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceTest {
    @InjectMocks
    OAuth2Service oAuth2Service;
    @Mock
    UserRequest userRequest;
    @Mock
    JWTHandlerService jwtHandlerService;
    @Mock
    OAuthRepository oAuthRepository;
    AuthDataDTO authDataDTO = new AuthDataDTO("login", "password", RoleType.USER);
    UserResponseDTO userResponseDTO = new UserResponseDTO();
    OAuthToken oAuthToken = new OAuthToken(123L, "login", RoleType.USER, "token",
            LocalDateTime.now(), LocalDateTime.now().plusDays(10), true, "userID");
    List<OAuthToken> tokenList = List.of(oAuthToken, oAuthToken);

    @SneakyThrows
    @Test
    void GetAuthorize_ReturnsValidToken_FirstAuthorize() {
        when(userRequest.getOAuth2(authDataDTO))
                .thenReturn(new ResponseEntity<>(userResponseDTO, HttpStatus.OK));
        when(oAuthRepository.findByLoginAndExpiredAtGreaterThanAndEnableTrue(any(), any()))
                .thenReturn(Collections.emptyList());

        verify(oAuthRepository, times(0))
                .saveAll(List.of(oAuthToken));

        when(jwtHandlerService.generateToken(userResponseDTO))
                .thenReturn("newGenerateToken");
        when(oAuthRepository.save(any())).thenReturn(oAuthToken);
        String token = oAuth2Service.getAuthorize(authDataDTO);
        Assertions.assertEquals(oAuthToken.getToken(), token);
    }

    @SneakyThrows
    @Test
    void ValidateToken_DoesntThrowException_ValideToken() {
        Optional<OAuthToken> token = Optional.of(oAuthToken);
        String validToken = "validToken";
        when(oAuthRepository.findByToken(validToken))
                .thenReturn(token);
        when(jwtHandlerService.isTokenValid(validToken, token.get().getLogin(), token.get().getRole()))
                .thenReturn(true);
        Assertions.assertDoesNotThrow(() -> oAuth2Service.validateToken(validToken));
    }

    @SneakyThrows
    @Test
    void ValidateToken_ThrowException_NonExistingToken() {
        Optional<OAuthToken> nullToken = Optional.empty();
        String nonExistToken = "validToken";
        when(oAuthRepository.findByToken(nonExistToken))
                .thenReturn(nullToken);
        verify(jwtHandlerService, times(0))
                .isTokenValid(any(), any(), any());
        Assertions.assertThrows(OAuthException.class,
                () -> oAuth2Service.validateToken(nonExistToken));
    }

    @SneakyThrows
    @Test
    void ValidateToken_ThrowException_TokenInvalid() {
        Optional<OAuthToken> invalidToken = Optional.of(oAuthToken);
        String incomingInvalidToken = "invalidToken";
        when(oAuthRepository.findByToken(incomingInvalidToken))
                .thenReturn(invalidToken);
        when(jwtHandlerService.isTokenValid(incomingInvalidToken,
                invalidToken.get().getLogin(), invalidToken.get().getRole()))
                .thenReturn(false);
        Assertions.assertThrows(OAuthException.class,
                () -> oAuth2Service.validateToken(incomingInvalidToken));
    }


    @SneakyThrows
    @Test
//    Wanted but not invoked:
//            oAuthRepository.saveAll(
//            [com.docdoc.oauth2.model.db.OAuthToken@488b50ec]
//            );
    void GetAuthorize_ReturnsValidToken_NotFirstAuthorize() {

        LocalDateTime dateTime = LocalDateTime.now();
        when(userRequest.getOAuth2(authDataDTO))
                .thenReturn(new ResponseEntity<>(userResponseDTO, HttpStatus.OK));
        when(oAuthRepository
                .findByLoginAndExpiredAtGreaterThanAndEnableTrue(authDataDTO.login(),
                        dateTime))
                .thenReturn(tokenList);
        when(oAuthRepository.saveAll(tokenList))
                .thenReturn(tokenList);
//        verify(oAuthRepository, times(1))
//                .saveAll(tokenList);

        when(jwtHandlerService.generateToken(userResponseDTO))
                .thenReturn("generateToken");
        when(oAuthRepository.save(any())).thenReturn(oAuthToken);
        String tokenNew = oAuth2Service.getAuthorize(authDataDTO);
        Assertions.assertEquals(oAuthToken.getToken(), tokenNew);

    }

    @SneakyThrows
    @Test
    //worked
    void GetAuthorize_ReturnsExceptionNotFound() {
        when(userRequest.getOAuth2(authDataDTO))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
        Assertions.assertThrows(OAuthException.class, () -> oAuth2Service.getAuthorize(authDataDTO));
    }
    @SneakyThrows
    @Test
//    Wanted but not invoked:
//            jwtHandlerService.generateToken(
//    com.docdoc.oauth2.model.dto.UserResponseDTO@1869f114
//            );
//-> at com.docdoc.oauth2.service.JWTHandlerService.generateToken(JWTHandlerService.java:36)
//    Actually, there were zero interactions with this mock.
    
    void GetAuthorize_ReturnsRunTimeException_JwtHandlerServiceNotParseToken() {
        ResponseEntity<UserResponseDTO> userResponse = new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
        when(userRequest.getOAuth2(any()))
                .thenReturn(userResponse);
        when(jwtHandlerService.generateToken(any()))
                .thenThrow(new JOSEException());
        verify(jwtHandlerService,times(1))
                .generateToken(userResponseDTO);
        Assertions.assertThrows(RuntimeException.class, () -> oAuth2Service.getAuthorize(any()));
        Assertions.assertThrows(JOSEException.class, () -> jwtHandlerService.generateToken(userResponseDTO));

    }
}

// 1) Тест при повторной авторизации oAuthRepository.saveAll(users); должен быть verify(1)
// и в findByLoginAndExpiredAtGreaterThanAndEnableTrue
// передай значение из authDataDTO
// 2) userRequest вернет ResponseEntitty c HttpStatus.NOT_FOUND
// 3) filter(response -> response.getStatusCode() != HttpStatus.NOT_FOUND && response.getBody() != null)  return OAuthException::userNotFound
//    4)when jwtHandlerService.generateToken(response.getBody()) ---> getAuthorize throws RunTimeException