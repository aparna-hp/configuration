package com.cisco.configService.exception;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IpAddressConstraintValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IpAddressConstraint {

    String message() default "IP address is invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
