package com.teleticket.auth.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {

    private KeyUtils() {
    }

    public static RSAPrivateKey loadPrivateKeyFromKeystore(String keystorePath, String keystorePassword, String alias, String keyPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = KeyUtils.class.getClassLoader().getResourceAsStream(keystorePath)) {
            if (inputStream == null) {
                throw new IOException("Keystore no encontrado en la ruta: " + keystorePath);
            }
            keyStore.load(inputStream, keystorePassword.toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword.toCharArray());
        if (privateKey == null) {
            throw new IllegalArgumentException("No se encontró la clave privada con el alias: " + alias);
        }
        return (RSAPrivateKey) privateKey;
    }

    public static RSAPublicKey loadPublicKeyFromKeystore(String keystorePath, String keystorePassword, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream inputStream = KeyUtils.class.getClassLoader().getResourceAsStream(keystorePath)) {
            if (inputStream == null) {
                throw new IOException("Keystore no encontrado en la ruta: " + keystorePath);
            }
            keyStore.load(inputStream, keystorePassword.toCharArray());
        }

        Certificate certificate = keyStore.getCertificate(alias);
        if (certificate == null) {
            throw new IllegalArgumentException("No se encontró el certificado con el alias: " + alias);
        }
        PublicKey publicKey = certificate.getPublicKey();
        return (RSAPublicKey) publicKey;
    }

    public static RSAPrivateKey loadPrivateKey(String path) throws Exception {
        InputStream inputStream = KeyUtils.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IOException("Clave privada no encontrada en la ruta: " + path);
        }

        String privateKeyPEM = new String(inputStream.readAllBytes());
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static RSAPublicKey loadPublicKey(String path) throws Exception {
        InputStream inputStream = KeyUtils.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IOException("Clave pública no encontrada en la ruta: " + path);
        }

        String publicKeyPEM = new String(inputStream.readAllBytes());
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);

        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }
}
