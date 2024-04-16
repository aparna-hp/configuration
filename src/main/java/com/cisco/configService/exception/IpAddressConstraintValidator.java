package com.cisco.configService.exception;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IpAddressConstraintValidator implements ConstraintValidator<IpAddressConstraint, String> {

    private static final String IPV4_REGEX =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValidInet4Address(String ip)
    {
        if (ip == null) {
            return false;
        }

        Matcher matcher = IPv4_PATTERN.matcher(ip);

        return matcher.matches();
    }

    @Override
    public void initialize(IpAddressConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        //Implement the logic here to validate IPv4 & IPv6 address.
        return isValidInet4Address(value);
    }
}
