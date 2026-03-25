package com.luisalt20.auth.controller;

import com.luisalt20.auth.config.JwtProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
public class DiscoveryController {

    private final RSAPublicKey publicKey;
    private final JwtProperties props;

    public DiscoveryController(KeyPair keyPair, JwtProperties props) {
        this.props = props;
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    @GetMapping("/.well-known/openid-configuration")
    public Map<String, Object> openidConfiguration() {
        return Map.of(
                "issuer", "http://localhost:8081",
                "jwks_uri", "http://localhost:8081/oauth2/jwks",
                "authorization_endpoint", "http://localhost:8081/oauth2/authorize",
                "token_endpoint", "http://localhost:8081/oauth2/token",
                "response_types_supported", new String[]{"code", "token"},
                "subject_types_supported", new String[]{"public"},
                "id_token_signing_alg_values_supported", new String[]{"RS256"}
        );
    }

    @GetMapping("/oauth2/jwks")
    public Map<String, Object> keys() {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .keyID(props.getUuid())
                .build();

        return new JWKSet(rsaKey).toJSONObject();
    }
}

