package ${basePackage}.core.repository.convertor;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.core.model.domain.${className};

/**
 * ${entityName} DO 与领域模型互转，只存在于 repository。
 * 显式 get/set 赋值：DO 与 Model 字段类型允许不同（如 status 字符串转枚举），业务方按需调整。
 */
public final class ${className}Convertor {

    private ${className}Convertor() {
    }

    /**
     * DO → 领域模型。
     *
     * @param ${classNameLower}DO ${entityName}数据对象；为空返回 null
     * @return ${entityName}领域模型
     */
    public static ${className} toModel(${className}DO ${classNameLower}DO) {
        if (${classNameLower}DO == null) {
            return null;
        }
        ${className} ${classNameLower} = new ${className}();
        ${classNameLower}.setId(${classNameLower}DO.getId());
<#list columns as c>
        ${classNameLower}.set${c.propertyName?cap_first}(${classNameLower}DO.get${c.propertyName?cap_first}());
</#list>        ${classNameLower}.setCreateTime(${classNameLower}DO.getCreateTime());
        ${classNameLower}.setUpdateTime(${classNameLower}DO.getUpdateTime());
        return ${classNameLower};
    }

    /**
     * 领域模型 → DO。
     *
     * @param ${classNameLower} ${entityName}领域模型
     * @return ${entityName}数据对象
     */
    public static ${className}DO toDO(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = new ${className}DO();
        ${classNameLower}DO.setId(${classNameLower}.getId());
<#list columns as c>
        ${classNameLower}DO.set${c.propertyName?cap_first}(${classNameLower}.get${c.propertyName?cap_first}());
</#list>        ${classNameLower}DO.setCreateTime(${classNameLower}.getCreateTime());
        ${classNameLower}DO.setUpdateTime(${classNameLower}.getUpdateTime());
        return ${classNameLower}DO;
    }
}
