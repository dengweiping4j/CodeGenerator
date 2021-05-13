package com.dwp.codegenerator.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CodecUtil {
    private static final Log log = LogFactory.getLog(CodecUtil.class);
    private static String key = "octopus123456789";
    private static String ivKey = "octopus12345iv67";

    /**
     * AES加密
     *
     * @param str 待加密的字符串
     * @return
     */
    public static String Encrypt(String str) {
        try {

            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

            byte[] encrypted = cipher.doFinal(str.getBytes());
            return Base64.encodeBase64String(encrypted);

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 解密
     *
     * @param encrypted 待解密的密文
     * @return
     */
    public static String Decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

}
