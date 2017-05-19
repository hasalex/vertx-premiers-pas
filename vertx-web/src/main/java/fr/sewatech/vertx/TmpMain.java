package fr.sewatech.vertx;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TmpMain {

    public static void main(String[] args) throws Exception {
//        simple();
        password();
    }

    private static void password() throws NoSuchAlgorithmException {
        String password = "aa";

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] bHash = md.digest(password.getBytes(StandardCharsets.UTF_8));

        System.out.println(bytesToHex1(bHash));
    }

    private static String bytesToHex1(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte x : bytes) {
            sb.append(String.format("%02X", x));
        }
        return sb.toString();
    }

}
