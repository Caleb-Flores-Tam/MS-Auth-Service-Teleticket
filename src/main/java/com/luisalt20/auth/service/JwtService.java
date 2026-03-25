package com.luisalt20.auth.service;

import com.luisalt20.auth.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtProperties props;

    public String issueAccessToken(Long userId, String email, List<String> roles) {
        var now = Instant.now();

        String jti = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.getAccessTtl()))
                .subject(email)
                .id(jti)
                .claim("uid", userId)
                .claim("roles", roles)
                .claim("aud", props.getAud())
                .build();
        var headers = JwsHeader.with(() -> "RS256")
                .keyId(props.getUuid())
                .build();
        return encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    public Map<String, Object> validate(String token) {
        var jwt = decoder.decode(token);
        return jwt.getClaims();
    }
}
