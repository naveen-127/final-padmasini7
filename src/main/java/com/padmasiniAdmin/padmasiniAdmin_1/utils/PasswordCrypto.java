package com.padmasiniAdmin.padmasiniAdmin_1.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Reversible (AES-GCM) copy of a password, ONLY so an admin can view it when editing a user. */
@Component
public class PasswordCrypto {

    // put app.password.view.key=SomeLongRandomSecret in application.properties
    @Value("${app.password.view.key:change-this-secret-key}")
    private String secret;

    private final SecureRandom random = new SecureRandom();

    private SecretKeySpec key() throws Exception {
        byte[] k = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(k, "AES");
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) return null;
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] enc = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(enc, 0, out, iv.length, enc.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            return null;
        }
    }

    public String decrypt(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            byte[] all = Base64.getDecoder().decode(data);
            byte[] iv = new byte[12];
            System.arraycopy(all, 0, iv, 0, 12);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            return new String(c.doFinal(all, 12, all.length - 12), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}