package com.cisco.configService.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ValidationService<T> {

    @Autowired
    Validator validator;

    private static final Logger logger =
            LogManager.getLogger(ValidationService.class);

    public void validateInput(T configParams) {
        if (null == configParams) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Configuration Parameters");
        }
        Set<ConstraintViolation<T>> violations = validator.validate(configParams);
        logger.info("Validation violations found : " + violations.size());
        if (!violations.isEmpty()) {
            violations.forEach(s -> logger.info(" Invalid value:" + s.getInvalidValue() + " "));
            throw new ConstraintViolationException(violations);
        }

    }
}
