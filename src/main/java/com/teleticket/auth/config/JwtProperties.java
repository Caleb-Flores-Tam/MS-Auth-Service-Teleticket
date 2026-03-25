package com.luisalt20.auth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Data
public class JwtProperties {

    @Value("${jwt.alg}")
    private String alg;

    @Value("${jwt.aud}")
    private String aud;

    @Value("${jwt.uuid}")
    private String uuid;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access-ttl}")
    private Duration accessTtl;

    @Value("${jwt.refresh-ttl}")
    private Duration refreshTtl;

    @Value("${jwt.validateSeconds}")
    private Integer validateSeconds;

    @Value("${jwt.nameTotp}")
    private String nameTotp;
}
