package com.teleticket.auth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;

@Service
public class TotpService {
    private final TimeBasedOneTimePasswordGenerator totp;

    public TotpService() throws Exception {
        this.totp = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30));
    }

    public Key generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(totp.getAlgorithm());
        keyGen.init(160);
        return keyGen.generateKey();
    }

    public boolean validateCode(Key key, int code) throws Exception {
        int current = totp.generateOneTimePassword(key, Instant.now());
        int previous = totp.generateOneTimePassword(key, Instant.now().minus(Duration.ofSeconds(30)));
        int next = totp.generateOneTimePassword(key, Instant.now().plus(Duration.ofSeconds(30)));

        // aceptar ventana +/-30s
        return (code == current || code == previous || code == next);
    }
}
