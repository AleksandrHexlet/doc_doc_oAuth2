package com.docdoc.oauth2.model.dto;

import com.docdoc.oauth2.configuration.RoleType;
import com.docdoc.oauth2.exception.OAuthException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class AuthDataDTO {
//    private String login;
//    private String password;
//    private RoleType role;
//}

public record AuthDataDTO(String login, String password,RoleType role){
//    public AuthDataDTO {
//        if(login.equals("test")){
//            throw new RuntimeException();
//        }
//    }
//
//    public AuthDataDTO(String login, String password, RoleType role) {
//        if(login.equals("test")){
//            throw new RuntimeException();
//        }
//        this.login = "111";
//        this.password = password;
//        this.role = role;
//
//    }
}
