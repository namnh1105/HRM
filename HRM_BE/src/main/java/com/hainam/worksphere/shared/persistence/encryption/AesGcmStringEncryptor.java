package com.hainam.worksphere.shared.persistence.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class AesGcmStringEncryptor {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmStringEncryptor(
            @Value("${app.security.encryption.key-base64:}") String keyBase64
    ) {
        this.secretKey = loadOrGenerateKey(keyBase64);
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return plainText;
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encrypt failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) return encryptedText;
        try {
            byte[] allBytes;
            try {
                allBytes = Base64.getDecoder().decode(encryptedText);
            } catch (IllegalArgumentException ex) {
                // Legacy/plaintext data (not encrypted) => return as-is
                return encryptedText;
            }

            // Must contain IV + GCM ciphertext/tag at minimum
            if (allBytes.length <= IV_LENGTH) {
                return encryptedText;
            }

            ByteBuffer buffer = ByteBuffer.wrap(allBytes);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            // GCM includes authentication tag in ciphertext payload.
            if (cipherBytes.length == 0) {
                return encryptedText;
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plainBytes = cipher.doFinal(cipherBytes);

            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decrypt failed for data (it might be encrypted with a different key or corrupted). Error: {}", e.getMessage());
            return null;
        }
    }

    private SecretKey loadOrGenerateKey(String keyBase64) {
        try {
            if (keyBase64 != null && !keyBase64.isBlank()) {
                byte[] key = Base64.getDecoder().decode(keyBase64);
                if (key.length != 16 && key.length != 24 && key.length != 32) {
                    throw new IllegalArgumentException("Invalid AES key length, expected 16/24/32 bytes");
                }
                return new SecretKeySpec(key, "AES");
            }

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize encryption key", e);
        }
    }
}
