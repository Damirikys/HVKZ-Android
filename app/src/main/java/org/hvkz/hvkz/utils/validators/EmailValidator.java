package org.hvkz.hvkz.utils.validators;

import java.util.regex.Pattern;

public class EmailValidator
{
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean emailAddressIsCorrect(String address) {
        return VALID_EMAIL_ADDRESS_REGEX.matcher(address).find();
    }
}
