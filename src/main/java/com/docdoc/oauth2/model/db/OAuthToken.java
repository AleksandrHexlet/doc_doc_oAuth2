package com.docdoc.oauth2.model.db;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import com.docdoc.oauth2.configuration.RoleType;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthToken {
    @Id
    @GeneratedValue
    private long id;
    private String login;
    private RoleType role;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private boolean enable;
    private String userId;

}
