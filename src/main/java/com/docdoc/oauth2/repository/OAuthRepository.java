package com.docdoc.oauth2.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docdoc.oauth2.model.db.OAuthToken;

public interface OAuthRepository extends JpaRepository<OAuthToken, Long> {

    List<OAuthToken> findByLoginAndExpiredAtGreaterThanAndEnableTrue(String login, LocalDateTime date);
    Optional<OAuthToken> findByToken(String token);
}
