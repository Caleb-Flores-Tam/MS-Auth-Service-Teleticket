package com.luisalt20.auth.service;

import com.luisalt20.auth.config.JwtProperties;
import com.luisalt20.auth.exception.ApiValidateException;
import com.luisalt20.auth.util.ConstantUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class TotpSessionService {
    private final Map<String, Instant> authorizedUsers = new HashMap<>();
    private final JwtProperties jwtProperties;

    public TotpSessionService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public void authorizeUser(String username) {
        authorizedUsers.put(username, Instant.now().plusSeconds(jwtProperties.getValidateSeconds()));
    }

    public boolean isUserAuthorized(String username) {
        Instant expiry = authorizedUsers.get(username);
        if (expiry == null) throw new ApiValidateException(ConstantUtil.MUST_VALIDATE);
        if (Instant.now().isAfter(expiry)) {
            authorizedUsers.remove(username);
            throw new ApiValidateException(ConstantUtil.MUST_VALIDATE);
        }
        return true;
    }

    public void revokeAuthorization(String username) {
        authorizedUsers.remove(username);
    }
}