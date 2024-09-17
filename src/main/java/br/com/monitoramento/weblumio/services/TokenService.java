package br.com.monitoramento.weblumio.services;

import br.com.monitoramento.weblumio.entities.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateAccessApiToken (User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("monitoramento-system")
                    .withSubject(user.getUsername())
                    .withExpiresAt(generateAccessApiTokenExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            return null;
        }
    }

    public String validateAccessApiToken (String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("monitoramento-system")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public String getUserUsernameByToken(String token) {
        if (token.startsWith("Bearer "))
            token = token.substring(7);
        else
            return null;

        return validateAccessApiToken(token);
    }

    private Instant generateAccessApiTokenExpirationDate () {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-12:00"));
    }

}
