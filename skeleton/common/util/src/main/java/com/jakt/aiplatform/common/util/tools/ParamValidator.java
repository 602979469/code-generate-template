package com.jakt.aiplatform.common.util.tools;

import com.jakt.aiplatform.common.util.error.CommonErrorCode;
import com.jakt.aiplatform.common.util.error.CommonException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * common-util 层参数校验工具。
 */
public final class ParamValidator {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ParamValidator() {
    }

    public static void validate(Object param, Class<?>... groups) {
        if (param == null) {
            return;
        }
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(param, groups);
        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        if (!message.isEmpty()) {
            throw CommonException.of(CommonErrorCode.PARAM_INVALID, message);
        }
    }
}
