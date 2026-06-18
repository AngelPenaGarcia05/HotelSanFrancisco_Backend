package com.sanfrancisco.api.modules.notificaciones.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Cifrado simétrico AES para la contraseña SMTP. A diferencia de BCrypt (usado para
 * contraseñas de login), este cifrado es reversible: necesitamos recuperar la
 * contraseña en texto plano para autenticarnos contra el servidor SMTP real.
 */
@Component
public class SmtpCredentialCipher {

    private static final String ALGORITHM = "AES";

    private final SecretKeySpec keySpec;

    public SmtpCredentialCipher(@Value("${app.notificaciones.smtp-cipher-key:default_smtp_cipher_key}") String rawKey) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            byte[] key16 = new byte[16];
            System.arraycopy(key, 0, key16, 0, 16);
            this.keySpec = new SecretKeySpec(key16, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar el cifrado de credenciales SMTP", e);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña SMTP", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo descifrar la contraseña SMTP", e);
        }
    }
}
