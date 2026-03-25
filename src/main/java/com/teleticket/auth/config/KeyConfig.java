package com.teleticket.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static com.teleticket.auth.util.KeyUtils.loadPrivateKeyFromKeystore;
import static com.teleticket.auth.util.KeyUtils.loadPublicKeyFromKeystore;

@Configuration
public class KeyConfig {

    private static final String CLASSPATH_PREFIX = "classpath:";

    @Value("${jwt.keystore.location}")
    private String keystoreLocation;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keystoreAlias;

    @Value("${jwt.keystore.key-password}")
    private String keyPassword;

    @Bean
    public KeyPair keyPair() throws Exception {
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
        return new KeyPair(publicKey, privateKey);
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        return loadPublicKeyFromKeystore(
                keystoreLocation.replace(CLASSPATH_PREFIX, ""),
                keystorePassword,
                keystoreAlias
        );
    }
}