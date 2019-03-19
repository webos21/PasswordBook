package com.gmail.webos21.passwordbook.crypt;

import com.gmail.webos21.crypto.Base64WebSafe;
import com.gmail.webos21.crypto.CryptoHelper;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class PbCryptHelper {

    public static String makeSha256Base64(String passkey) {
        byte[] pkBytes = CryptoHelper.sha2hash(passkey, "ASCII");
        return Base64WebSafe.encode(pkBytes);
    }

    public static byte[] restorePkBytes(String passkey) {
        return Base64WebSafe.decode(passkey);
    }

    public static boolean checkPasskey(String passkey, byte[] pkBytes) {
        byte[] chkBytes = CryptoHelper.sha2hash(passkey, "ASCII");
        return Arrays.equals(chkBytes, pkBytes);
    }

    public static String encData(String plainData, byte[] pkBytes) {
        byte[] srcData = null;
        try {
            srcData = plainData.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (pkBytes == null || pkBytes.length != 32) {
            throw new IllegalArgumentException("pkBytes is not valid!!!");
        }

        byte[] iv = new byte[16];
        System.arraycopy(pkBytes, 0, iv, 0, 16);

        byte[] encData = CryptoHelper.encryptWithAESKey(pkBytes, iv, srcData);
        return Base64WebSafe.encode(encData);
    }

    public static String decData(String cryptData, byte[] pkBytes) {
        byte[] srcData = Base64WebSafe.decode(cryptData);

        if (pkBytes == null || pkBytes.length != 32) {
            throw new IllegalArgumentException("pkBytes is not valid!!!");
        }

        byte[] iv = new byte[16];
        System.arraycopy(pkBytes, 0, iv, 0, 16);

        byte[] decData = CryptoHelper.decryptWithAESKey(pkBytes, iv, srcData);
        String result = null;
        try {
            result = new String(decData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

}
