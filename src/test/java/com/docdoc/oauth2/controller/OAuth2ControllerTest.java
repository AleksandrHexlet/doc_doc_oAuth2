package com.docdoc.oauth2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import com.docdoc.oauth2.configuration.RoleType;
import com.docdoc.oauth2.exception.OAuthException;
import com.docdoc.oauth2.model.dto.AuthDataDTO;
import com.docdoc.oauth2.service.OAuth2Service;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OAuth2Controller.class)
class OAuth2ControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    OAuth2Controller oAuth2Controller;

    @MockBean
    OAuth2Service oAuth2Service;
    String tokenValue = "1234567";
    AuthDataDTO authDataDTO = new AuthDataDTO("login", "password", RoleType.USER);

    @SneakyThrows
    private String getJSONFromAuthDataDTO(AuthDataDTO authDataDTO) {
        return new ObjectMapper().writeValueAsString(authDataDTO);
    }


    @SneakyThrows
    @Test
    void validateToken_ReturnBadRequest_EmptyToken() {
        mockMvc.perform(post("/oAuth2/token/validate")
                        .contentType("application/json")
                        .queryParam("token", ""))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void validateToken_ReturnBadRequest_BlankToken() {
        mockMvc.perform(post("/oAuth2/token/validate")
                        .queryParam("token", " "))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void validateToken_ReturnStatusOK() {
        doNothing().when(oAuth2Service)
                .validateToken("1234567");
        mockMvc.perform(post("/oAuth2/token/validate")
                        .queryParam("token", tokenValue))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void validateToken_ReturnResponseStatusException_oAuth2ServiceThrowOAuthException() {
        doThrow(OAuthException.class)
                .when(oAuth2Service).validateToken(tokenValue);
        mockMvc.perform(post("/oAuth2/token/validate"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    private void invalidLoginTest(AuthDataDTO authDataDTO) {
        mockMvc.perform(post("/oAuth2/authorize")
                        .contentType("application/json")
                        .content(getJSONFromAuthDataDTO(authDataDTO)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void getOAuth2_ReturnBadRequest_InvalidLogin() {
        AuthDataDTO invalidAuthData = new AuthDataDTO(null, "password", RoleType.USER);
        invalidLoginTest(invalidAuthData);

        invalidAuthData = new AuthDataDTO("", "password", RoleType.USER);
        invalidLoginTest(invalidAuthData);

        invalidAuthData = new AuthDataDTO(" ", "password", RoleType.USER);
        invalidLoginTest(invalidAuthData);
    }

    @SneakyThrows
    @Test
    void getOAuth2_ReturnBadRequest_InvalidPassword() {
        AuthDataDTO invalidAuthData = new AuthDataDTO("login", null, RoleType.USER);
        invalidLoginTest(invalidAuthData);

        invalidAuthData = new AuthDataDTO("login", "", RoleType.USER);
        invalidLoginTest(invalidAuthData);

        invalidAuthData = new AuthDataDTO("login", " ", RoleType.USER);
        invalidLoginTest(invalidAuthData);
    }

    @SneakyThrows
    @Test
    void getOAuth2_ReturnBadRequest_InvalidRoleType() {
        AuthDataDTO invalidAuthData = new AuthDataDTO("login", "password", null);
        invalidLoginTest(invalidAuthData);
    }

    @SneakyThrows
    @Test
    void getOAuth2_ReturnStatusOK() {
        when(oAuth2Service.getAuthorize(authDataDTO))
                .thenReturn("token");
        mockMvc.perform(post("/oAuth2/authorize")
                        .contentType("application/json")
                        .content(getJSONFromAuthDataDTO(authDataDTO)))
                .andExpect(content().string("token"));
    }
    @SneakyThrows
    @Test
    void getOAuth2_ReturnNotFound() {
        when(oAuth2Service.getAuthorize(authDataDTO))
                .thenThrow(OAuthException.userNotFound());
        mockMvc.perform(post("/oAuth2/authorize")
                        .contentType("application/json")
                        .content(getJSONFromAuthDataDTO(authDataDTO)))
                .andExpect(status().isNotFound());
    }
    @SneakyThrows
    @Test
    void getOAuth2_ReturnRuntime() {
        when(oAuth2Service.getAuthorize(authDataDTO))
                .thenThrow(RuntimeException.class);
        mockMvc.perform(post("/oAuth2/authorize")
                        .contentType("application/json")
                        .content(getJSONFromAuthDataDTO(authDataDTO)))
                .andExpect(status().isInternalServerError());
    }
}