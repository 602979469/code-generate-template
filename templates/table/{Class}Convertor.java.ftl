package ${basePackage}.core.repository.convertor;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.core.model.domain.${className};
${convertorImports}

/**
 * ${entityName} DO 与领域模型互转，只存在于 repository。
 * 显式 get/set 赋值：DO 保持数据库原始类型，Model 按列级配置转换（枚举 / json / 强制类型）。
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
    public static ${className} toModel(${className}DO source) {
        if (source == null) {
            return null;
        }
        ${className} target = new ${className}();
        target.setId(source.getId());
<#list columns as c>
        target.set${c.propertyName?cap_first}(${c.toModelExpr?replace("{do}", "source")});
</#list>        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    /**
     * 领域模型 → DO。
     *
     * @param ${classNameLower} ${entityName}领域模型
     * @return ${entityName}数据对象
     */
    public static ${className}DO toDO(${className} source) {
        ${className}DO target = new ${className}DO();
        target.setId(source.getId());
<#list columns as c>
        target.set${c.propertyName?cap_first}(${c.toDoExpr?replace("{model}", "source")});
</#list>        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }
}
