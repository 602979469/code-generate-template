package ${basePackage}.web.checker;

import cn.hutool.core.util.ObjectUtil;
import ${basePackage}.common.util.tools.${toolPrefix}Invoker;
import ${basePackage}.common.util.tools.${toolPrefix}ParamValidator;
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.web.param.${className}CreateRequest;
import ${basePackage}.web.param.${className}QueryRequest;
import ${basePackage}.web.param.${className}UpdateRequest;

/**
 * ${entityName}参数检查器
 */
public class ${className}ParamChecker {

    private ${className}ParamChecker() {
    }

    /**
     * 检查${entityName}创建参数。
     *
     * @param request ${entityName}创建请求
     */
    public static void check${className}CreateRequest(${className}CreateRequest request) {
        ${toolPrefix}Invoker.throwErrWhenNull(request, ErrorCodeEnum.PARAM_INVALID, "创建参数不能为空");
        ${toolPrefix}ParamValidator.validate(request);
    }

    /**
     * 检查${entityName}更新参数。
     *
     * @param request ${entityName}更新请求
     */
    public static void check${className}UpdateRequest(${className}UpdateRequest request) {
        ${toolPrefix}Invoker.throwErrWhenNull(request, ErrorCodeEnum.PARAM_INVALID, "更新参数不能为空");
        ${toolPrefix}ParamValidator.validate(request);
    }

    /**
     * 检查${entityName} ID 参数（按 ID 查询/删除共用）。
     *
     * @param id ${entityName} ID
     */
    public static void checkId(${pkJavaType} id) {
        ${toolPrefix}Invoker.throwErrWhenNull(id, ErrorCodeEnum.PARAM_INVALID, "${entityName}ID不能为空");
    }

    /**
     * 检查${entityName}查询参数
     *
     * @param request ${entityName}查询请求，可为 null
     */
    public static void check${className}QueryRequest(${className}QueryRequest request) {
        if (ObjectUtil.isNull(request)) {
            return;
        }
        ${toolPrefix}ParamValidator.validate(request);
    }
}
