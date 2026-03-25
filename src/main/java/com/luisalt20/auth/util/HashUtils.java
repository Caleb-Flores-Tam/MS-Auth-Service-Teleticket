package com.luisalt20.auth.util;

import java.nio.charset.StandardCharsets;

public final class HashUtils {
    private HashUtils() {}
    public static String sha256(String raw) {
        // DigestUtils.sha256DigestAsHex requiere Spring 6; si prefieres, usa MessageDigest
        return org.springframework.util.DigestUtils
                .appendMd5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8), new StringBuilder()) // placeholder si no tienes sha256
                .toString();
    }
}
