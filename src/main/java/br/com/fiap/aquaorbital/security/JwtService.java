package br.com.fiap.aquaorbital.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${aquaorbital.jwt.secret}")
    private String secret;

    @Value("${aquaorbital.jwt.expiration}")
    private long expiration;

    public String gerarToken(UserDetails userDetails) {
        return gerarToken(new HashMap<>(), userDetails);
    }

    public String gerarToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)                                              // setClaims → claims
                .subject(userDetails.getUsername())                               // setSubject → subject
                .issuedAt(new Date(System.currentTimeMillis()))                   // setIssuedAt → issuedAt
                .expiration(new Date(System.currentTimeMillis() + expiration))    // setExpiration → expiration
                .signWith(getSigningKey())                                         // removido SignatureAlgorithm (inferido automaticamente)
                .compact();
    }

    public boolean isTokenValido(String token, UserDetails userDetails) {
        final String username = extrairUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpirado(token);
    }

    public String extrairUsername(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public <T> T extrairClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extrairTodosClaims(token));
    }

    private boolean isTokenExpirado(String token) {
        return extrairClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extrairTodosClaims(String token) {
        return Jwts.parser()                          // parserBuilder() → parser()
                .verifyWith(getSigningKey())           // setSigningKey → verifyWith
                .build()
                .parseSignedClaims(token)             // parseClaimsJws → parseSignedClaims
                .getPayload();                         // getBody → getPayload
    }

    private SecretKey getSigningKey() {               // Key → SecretKey
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}