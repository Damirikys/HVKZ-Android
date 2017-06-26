package org.hvkz.hvkz.auth;

public final class NumberValidator
{
    private static final int USERNAME_FIXED_LENGTH = 10;

    public static boolean numberIsCorrect(String number) {
        return number.length() == USERNAME_FIXED_LENGTH;
    }
}
