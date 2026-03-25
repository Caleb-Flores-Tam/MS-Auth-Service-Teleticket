package com.luisalt20.auth.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static com.luisalt20.auth.util.KeyUtils.loadPrivateKeyFromKeystore;
import static com.luisalt20.auth.util.KeyUtils.loadPublicKeyFromKeystore;

@Configuration
public class JwtConfig {

    private static final String CLASSPATH_PREFIX = "classpath:";

    @Value("${jwt.uuid}")
    private String uuid;

    @Value("${jwt.keystore.location}")
    private String keystoreLocation;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keystoreAlias;

    @Value("${jwt.keystore.key-password}")
    private String keyPassword;

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        RSAPrivateKey privateKey = loadPrivateKeyFromKeystore(
                keystoreLocation.replace(CLASSPATH_PREFIX, ""),
                keystorePassword,
                keystoreAlias,
                keyPassword
        );
        RSAPublicKey publicKey = loadPublicKeyFromKeystore(
                keystoreLocation.replace(CLASSPATH_PREFIX, ""),
                keystorePassword,
                keystoreAlias
        );
        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(uuid)
                .build();
        JWKSet jwkSet = new JWKSet(jwk);
        JWKSource<SecurityContext> jwkSource = (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey publicKey = loadPublicKeyFromKeystore(
                keystoreLocation.replace(CLASSPATH_PREFIX, ""),
                keystorePassword,
                keystoreAlias
        );
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
