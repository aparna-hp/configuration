package com.cisco.configService.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class CryptoServiceTest {

    @Autowired
    CryptoService cryptoService;

    private static final Logger logger =
            LogManager.getLogger(CryptoServiceTest.class);

    @Test
    public void testEncryption() throws Exception {
        String secret = "cisco";
        byte[] encrypted = cryptoService.aesEncrypt(secret);
        logger.info("encrypted " + Arrays.toString(encrypted));
        byte[] decrypted = cryptoService.aesDecrypt(encrypted);
        logger.info("decrypted " + new String(decrypted));
        Assertions.assertEquals(new String(decrypted), secret);
    }
}
