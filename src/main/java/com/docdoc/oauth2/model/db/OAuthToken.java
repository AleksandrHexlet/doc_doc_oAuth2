package com.docdoc.oauth2.model.db;

import com.docdoc.oauth2.configuration.RoleType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthToken {
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
