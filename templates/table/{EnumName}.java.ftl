package ${pkgEnums};

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import ${basePackage}.common.framework.enums.BaseEnum;
import lombok.Getter;

/**
 * ${entityName}${enumDesc}枚举。
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ${enumClassName} implements BaseEnum<${enumCodeType}> {

<#list enumValues as enumValue>
    /** ${enumValue.desc}。 */
    ${enumValue.name}(${enumValue.codeLiteral}, "${enumValue.desc}"),
</#list>
    ;

    /** code（数据库存储值）。 */
    private final ${enumCodeType} code;

    /** 描述。 */
    private final String desc;

    ${enumClassName}(${enumCodeType} code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public ${enumCodeType} getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }

    /**
     * 按 code 反查枚举；Jackson 反序列化入口。
     *
     * @param code code
     * @return 枚举
     */
    @JsonCreator
    public static ${enumClassName} fromCodeJson(String code) {
<#if enumCodeType == "Integer">
        return code == null ? null : BaseEnum.fromCode(${enumClassName}.class, Integer.valueOf(code));
<#elseif enumCodeType == "Long">
        return code == null ? null : BaseEnum.fromCode(${enumClassName}.class, Long.valueOf(code));
<#else>
        return BaseEnum.fromCode(${enumClassName}.class, code);
</#if>
    }

    /**
     * 按 code 反查枚举（业务代码/Convertor 使用）。
     *
     * @param code code
     * @return 枚举
     */
    public static ${enumClassName} fromCode(${enumCodeType} code) {
        return BaseEnum.fromCode(${enumClassName}.class, code);
    }
}
