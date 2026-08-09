package com.jakt.aiplatform.web.checker;

import cn.hutool.core.util.ObjectUtil;
import com.jakt.aiplatform.common.util.tools.AiPlatformInvoker;
import com.jakt.aiplatform.common.util.tools.AiPlatformParamValidator;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.web.param.UserCreateRequest;
import com.jakt.aiplatform.web.param.UserQueryRequest;
import com.jakt.aiplatform.web.param.UserUpdateRequest;

/**
 * 用户参数检查器
 */
public class UserParamChecker {

    private UserParamChecker() {
    }

    /**
     * 检查用户创建参数。
     *
     * @param request 用户创建请求
     */
    public static void checkUserCreateRequest(UserCreateRequest request) {
        AiPlatformInvoker.throwErrWhenNull(request, ErrorCodeEnum.PARAM_INVALID, "创建参数不能为空");
        AiPlatformParamValidator.validate(request);
    }

    /**
     * 检查用户更新参数。
     *
     * @param request 用户更新请求
     */
    public static void checkUserUpdateRequest(UserUpdateRequest request) {
        AiPlatformInvoker.throwErrWhenNull(request, ErrorCodeEnum.PARAM_INVALID, "更新参数不能为空");
        AiPlatformParamValidator.validate(request);
    }

    /**
     * 检查用户 ID 参数（按 ID 查询/删除共用）。
     *
     * @param id 用户 ID
     */
    public static void checkId(Long id) {
        AiPlatformInvoker.throwErrWhenNull(id, ErrorCodeEnum.PARAM_INVALID, "用户ID不能为空");
    }

    /**
     * 检查用户查询参数
     *
     * @param request 用户查询请求，可为 null
     */
    public static void checkUserQueryRequest(UserQueryRequest request) {
        if (ObjectUtil.isNull(request)) {
            return;
        }
        AiPlatformParamValidator.validate(request);
    }
}
