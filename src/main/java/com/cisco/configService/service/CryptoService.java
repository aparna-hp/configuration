package com.cisco.configService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
@Slf4j
public class CryptoService {

    private static final byte[] aesKey = {
            (byte) 14, (byte) 171, (byte) 167, (byte) 51,
            (byte) 142, (byte) 139, (byte) 119, (byte) 18,
            (byte) 140, (byte) 184, (byte) 145, (byte) 220,
            (byte) 15, (byte) 224, (byte) 194, (byte) 161
    };
    private static final byte[] aesIv = {
            (byte) 233, (byte) 229, (byte) 105, (byte) 120,
            (byte) 192, (byte) 79, (byte) 4, (byte) 176,
            (byte) 64, (byte) 215, (byte) 177, (byte) 162,
            (byte) 2, (byte) 30, (byte) 51, (byte) 20
    };

    //For AES encryption
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORM = "AES/CBC/PKCS5PADDING";

    /**
     * Encrypt the plain text
     */
    public byte[] aesEncrypt(String plainText) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, AES_ALGORITHM);
        IvParameterSpec ips = new IvParameterSpec(aesIv);
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ips);
            return cipher.doFinal(plainText.getBytes());
        } catch (Exception e) {
            log.error("Error encoding contents", e);
        }
        return null;
    }

    /**
     * Decrypt the secret
     */
    public byte[] aesDecrypt(byte[] secret) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, AES_ALGORITHM);
        IvParameterSpec ips = new IvParameterSpec(aesIv);
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ips);
            return cipher.doFinal(secret);
        } catch (Exception e) {
            log.error("Error decoding contents", e);
        }
        return null;
    }
}
