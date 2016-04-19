package org.jnode.rest.core;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class CryptoUtils {

    public static void main(String[] args) {
        System.out.println(randomToken());
    }

    private CryptoUtils() {
    }

    public static String makeToken(String pwd) {
        return sha256(pwd + "lamerskayaSoll");
    }

    private static String sha256(String protocolPassword) {
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("SHA-256");
            mdEnc.update(protocolPassword.getBytes(), 0,
                    protocolPassword.length());
            String s = new BigInteger(1, mdEnc.digest()).toString(16);
            return "SHA256-" + s;
        } catch (NoSuchAlgorithmException e) {
            return "PLAIN-" + protocolPassword;
        }

    }

    public static String randomToken(){
        return new BigInteger(160, new SecureRandom()).toString(Character.MAX_RADIX);
    }


}
