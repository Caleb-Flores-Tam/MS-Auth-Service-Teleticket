package com.luisalt20.auth.controller;

import com.luisalt20.auth.config.JwtProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
public class JwksController {

    private final RSAPublicKey publicKey;
    private final JwtProperties props;

    public JwksController(RSAPublicKey publicKey, JwtProperties props) {
        this.props = props;
        this.publicKey = publicKey;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(props.getUuid())
                .build();
        return new JWKSet(rsaKey).toJSONObject();
    }
}
