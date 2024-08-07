package com.docdoc.oauth2.repository;

import com.docdoc.oauth2.model.db.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuthToken, Long> {

   List<OAuthToken> findByLoginAndExpiredAtGreatThenDateAndEnableTrue(String login, LocalDate date);
   Optional<OAuthToken> findByToken(String token);
}
