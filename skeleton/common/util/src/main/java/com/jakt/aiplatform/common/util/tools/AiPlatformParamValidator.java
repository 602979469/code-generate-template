package com.jakt.aiplatform.common.util.tools;

import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 参数校验器：校验入参对象属性上的注解（@NotBlank/@Size/@NotNull 等），供 web 层模板等统一调用。
 *
 * <p>规则：入参为空直接跳过不校验；校验失败抛 {@link com.jakt.aiplatform.core.model.exception.AiPlatformException}（PARAM_INVALID）。
 */
public final class AiPlatformParamValidator {

    /** jakarta validation 校验器，应用生命周期内复用。 */
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private AiPlatformParamValidator() {
    }

    /**
     * 校验对象属性上的注解。
     *
     * @param param  入参对象；为空直接返回不校验
     * @param groups 校验组（可选），不传只校验默认组
     */
    public static void validate(Object param, Class<?>... groups) {
        if (param == null) {
            return;
        }
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(param, groups);
        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        AiPlatformInvoker.throwErrWhenNotBlank(
                message,
                ErrorCodeEnum.PARAM_INVALID,
                message);
    }
}
