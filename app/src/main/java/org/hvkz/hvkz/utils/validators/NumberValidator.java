package org.hvkz.hvkz.utils.validators;

public final class NumberValidator
{
    private static final int USERNAME_FIXED_LENGTH = 10;

    public static boolean numberIsCorrect(String number) {
        return number.length() == USERNAME_FIXED_LENGTH && number.matches("[0-9]+");
    }
}
