package com.docdoc.oauth2.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.docdoc.oauth2.configuration.RoleType;
import com.docdoc.oauth2.model.db.OAuthToken;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatList;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OAuthRepositoryTest {
    @Autowired
    OAuthRepository oAuthRepository;
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");
    OAuthToken oAuthToken1;
    OAuthToken oAuthToken2;
    OAuthToken oAuthToken3;

    @DynamicPropertySource
    static void configureProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        System.out.println(" postgreSQLContainer::getJdbcUrl :: " +  postgreSQLContainer.getJdbcUrl());
        System.out.println(" postgreSQLContainer::getUsername :: " +  postgreSQLContainer.getUsername());
        System.out.println(" postgreSQLContainer::getPassword :: " +  postgreSQLContainer.getPassword());
    }

    @BeforeEach
    void init() {
        oAuthToken1 = OAuthToken.builder().id(1l).token("token1").enable(true).login("login")
                .role(RoleType.USER).userId("userID").createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(15)).build();
        oAuthToken2 = OAuthToken.builder().id(2l).token("token2").enable(false).login("falseLogin")
                .role(RoleType.USER).userId("userID").createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(15)).build();
        oAuthToken3 = OAuthToken.builder().id(3l).token("token3").enable(true).login("login")
                .role(RoleType.USER).userId("userID").createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(15)).build();
        oAuthRepository.deleteAll();
        oAuthRepository.save(oAuthToken1);
        oAuthRepository.save(oAuthToken2);
        oAuthRepository.save(oAuthToken3);
    }


    @Test
    void FindByToken_ReturnsOauthToken_TokenExists() {
        Optional<OAuthToken> token = oAuthRepository.findByToken("token1");
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isEmpty());
        Assertions.assertEquals(oAuthToken1.getToken(), token.get().getToken());
    }


    @Test
    void findByLoginAndExpiredAtGreaterThanAndEnableTrue() {
        List<Long> tokenList = oAuthRepository.findByLoginAndExpiredAtGreaterThanAndEnableTrue("login",
                LocalDateTime.now()).stream().map(oAuthToken -> oAuthToken.getId()).toList();
        System.out.println("tokenList :: " + tokenList);
        Assertions.assertEquals(tokenList.size(), 2);
        assertThatList(tokenList).isEqualTo(List.of(1l, 3l));
    }



}
