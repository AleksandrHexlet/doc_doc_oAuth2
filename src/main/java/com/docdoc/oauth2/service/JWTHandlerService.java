package com.docdoc.oauth2.service;

import com.docdoc.oauth2.configuration.RoleType;
import com.docdoc.oauth2.model.dto.AuthDataDTO;
import com.docdoc.oauth2.model.dto.UserResponseDTO;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JWTHandlerService {
    @Value("${ru.docdoc.oauth.secret.key.token}")
    private String secretKey;
    @Value("${ru.docdoc.oauth.life.span.token}")
    private int lifetimeJWTtoken;
    private final WebApplicationContext webApplicationContext;
    private final ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor;

    public JWTHandlerService(WebApplicationContext webApplicationContext, ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor) {
        this.webApplicationContext = webApplicationContext;
        this.jwtProcessor = jwtProcessor;
    }

    public String generateToken(UserResponseDTO dataDTO) throws JOSEException {
        JWEObject jweObject = new JWEObject(
                getHeader(),
                getPayload(dataDTO));
        encrypt(jweObject);
        return jweObject.serialize();
    }


    private JWEHeader getHeader() {
        return new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256);
    }

    private Payload getPayload(UserResponseDTO responseDTO) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(responseDTO.getLogin()) // идентификатор пользователя subject
                .claim("name", responseDTO.getName())
                .claim("lastName", responseDTO.getLastName())
                .claim("city", responseDTO.getCity())
                .claim("area", responseDTO.getAreaName())
                .claim("metroStationName", responseDTO.getMetroStationName())
                .claim("userId", responseDTO.getUserId())
                .claim("role", responseDTO.getRole())
                .issueTime(new Date()) // время выдачи токена
                .expirationTime(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(lifetimeJWTtoken)))
                .build(); // TODO продолжаем тут(страница 34). Выясни как сформировать
        //  TODO .expirationTime(new Date(System.currentTimeMillis() + expireAtJWTtoken))
        // TODO так как плохо складывать миллисекунды с днями
        return new Payload(claims.toJSONObject());
    }

    // TODO выясни подробнее что происходит в методах ниже
    public JWEEncrypter encrypter() throws KeyLengthException {
        return new DirectEncrypter(secretKey.getBytes());
    }

    private void encrypt(JWEObject jweObject) throws JOSEException {
        jweObject.encrypt(encrypter());
    }


//    public String generateRefreshToken() {
//        return UUID.randomUUID().toString();
//    }

    private JWTClaimsSet extractClaims(String token) throws BadJOSEException, ParseException, JOSEException {
        ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor =
                (ConfigurableJWTProcessor<SimpleSecurityContext>) webApplicationContext.getBean("JWTProcessor");
        return jwtProcessor.process(token, null);
    }
    public boolean isTokenValid(String token, String login,RoleType roleType) throws BadJOSEException, ParseException, JOSEException {
        JWTClaimsSet claims = extractClaims(token);
        String subject = claims.getSubject();
        Date expiration = claims.getExpirationTime();
        String userId = claims.getStringClaim("userId");
        RoleType type = RoleType.valueOf(claims.getStringClaim("role"));

        return login.equals(subject) && expiration.after(new Date()) && type.equals(roleType);
    }

    public String getStringClaimsByKey(String token, String claimsKey) throws BadJOSEException, ParseException, JOSEException {
        JWTClaimsSet claims = extractClaims(token);
        return claims.getStringClaim(claimsKey);
    }

    public String getSubject(String token) throws BadJOSEException, ParseException, JOSEException {
        JWTClaimsSet claims = extractClaims(token);
        return claims.getSubject();
    }



}

//package com.edu.ishop.client.services;
//
//import com.nimbusds.jose.*;
//import com.nimbusds.jose.crypto.DirectEncrypter;
//import com.nimbusds.jose.jwk.source.ImmutableSecret;
//import com.nimbusds.jose.jwk.source.JWKSource;
//import com.nimbusds.jose.proc.BadJOSEException;
//import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
//import com.nimbusds.jose.proc.JWEKeySelector;
//import com.nimbusds.jose.proc.SimpleSecurityContext;
//import com.nimbusds.jwt.JWTClaimsSet;
//import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
//import com.nimbusds.jwt.proc.DefaultJWTProcessor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.text.ParseException;
//import java.util.Date;
//import java.util.UUID;
//
//
//@Service
//public class JwtSecurityService {
//    private final WebApplicationContext webApplicationContext;
//    @Value("${iShop.jwtSecret}")
//    private String jwtSecret;
//    @Value("${iShop.jwtRefreshSecret}")
//    private String jwtRefreshSecret;
//    @Value("${iShop.jwtSecretExpiration}")
//    private long jwtSecretExpiration;
//
//
//    @Autowired
//    public JwtSecurityService(WebApplicationContext webApplicationContext) {
//        this.webApplicationContext = webApplicationContext;
//    }
//
//    private JWEHeader getHeader() {
//        return new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256);
//    }
//
//    private Payload getPayload(String subject, String name) {
//        JWTClaimsSet claims = new JWTClaimsSet.Builder()
//                .subject(subject)
//                .claim("name", name)
//                .issueTime(new Date())
//                .expirationTime(new Date(System.currentTimeMillis() + jwtSecretExpiration))
//                .build();
//
//        return new Payload(claims.toJSONObject());
//    }
//
//    private void encrypt(JWEObject jweObject) throws JOSEException {
//        jweObject.encrypt((JWEEncrypter) webApplicationContext.getBean("JWEEncrypter"));
////        jweObject.encrypt((JWEEncrypter) webApplicationContext.getBean("JWEEncrypter"));
//    }
//
//    public String generateToken(CustomerUserDetails customerUserDetails) throws JOSEException {
//        JWEObject jweObject = new JWEObject(
//                getHeader(),
//                getPayload(
//                        customerUserDetails.getUsername(),
//                        customerUserDetails.getCustomer().getName()));
//
//        encrypt(jweObject);
//        return jweObject.serialize();
//    }
//
//    @Bean("JWEEncrypter")
//    public JWEEncrypter encrypter() throws KeyLengthException {
//        return new DirectEncrypter(jwtSecret.getBytes());
//    }
//
//    @Bean("JWTProcessor")
//    public ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor() {
//        ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
//        JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<>(jwtSecret.getBytes());
//        JWEKeySelector<SimpleSecurityContext> jweKeySelector =
//                new JWEDecryptionKeySelector<>(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256, jweKeySource);
//        jwtProcessor.setJWEKeySelector(jweKeySelector);
//        return jwtProcessor;
//    }
//
//    public String generateRefreshToken() {
//        return UUID.randomUUID().toString();
//    }
//
//    private JWTClaimsSet extractClaims(String token) throws BadJOSEException, ParseException, JOSEException {
//        ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor =
//                (ConfigurableJWTProcessor<SimpleSecurityContext>) webApplicationContext.getBean("JWTProcessor");
//        return jwtProcessor.process(token, null);
//    }
//
//    public String getSubject(String token) throws BadJOSEException, ParseException, JOSEException {
//        JWTClaimsSet claims = extractClaims(token);
//        return claims.getSubject();
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) throws BadJOSEException, ParseException, JOSEException {
//        String userName = getSubject(token);
//        Date expiration = extractClaims(token).getExpirationTime();
//        return userName.equals(userDetails.getUsername()) && expiration.after(new Date());
//    }
//
//    public String getStringClaimsByKey(String token, String claimsKey) throws BadJOSEException, ParseException, JOSEException {
//        JWTClaimsSet claims = extractClaims(token);
//        return claims.getStringClaim(claimsKey);
//    }
//
//    /*
//    import io.jsonwebtoken.Claims;
//    import io.jsonwebtoken.Jwts;
//    import io.jsonwebtoken.SignatureAlgorithm;
//    import io.jsonwebtoken.io.Decoders;
//    import io.jsonwebtoken.security.Keys;
//    import java.security.Key;
//    import java.util.Date;
//    import java.util.HashMap;
//    import java.util.Map;
//    import java.util.Random;
//    import java.util.function.Function;
//    */
//    /*
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
//        Claims claims = extractAllClaims(token);
//        return resolver.apply(claims);
//    }
//
//    public String generateToken(UserDetails userDetails) {
//        return generateToken(new HashMap<>(), userDetails);
//    }
//
//    public String generateToken(
//            Map<String, Object> extraClaims, UserDetails userDetails
//    ) {
//        System.out.println(jwtSecret);
//        return Jwts.builder()
//                .setClaims(extraClaims).setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + jwtSecretExpiration))
//                .signWith(getSigningKey(jwtSecret), SignatureAlgorithm.HS256).compact();
//    }
//    public String generateRefreshToken() {
//        System.out.println(jwtRefreshSecret);
//        Random randomChar = new Random();
//        char charRandom = (char)(randomChar.nextInt(26) + 'a');
//        return Jwts.builder()
//                .signWith(getSigningKey(jwtRefreshSecret + charRandom), SignatureAlgorithm.HS256).compact();
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    private Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    private Claims extractAllClaims(String token) {
//        return Jwts
//                .parserBuilder().setSigningKey(getSigningKey(jwtSecret))
//                .build().parseClaimsJws(token)
//                .getBody();
//    }
//
//    private Key getSigningKey(String jwtSecret) {
//        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//    */
//
//
//}
