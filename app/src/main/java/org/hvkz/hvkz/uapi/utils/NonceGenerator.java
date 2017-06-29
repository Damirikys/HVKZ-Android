package org.hvkz.hvkz.uapi.utils;

import java.security.SecureRandom;
import java.util.Random;

public final class NonceGenerator
{
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final int NONCE_LENGTH = 30;

    public static String nonce() {
        Random random = new SecureRandom();

        StringBuilder sb = new StringBuilder(NONCE_LENGTH);
        for (int i = 0; i < NONCE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return sb.toString();
    }
}
