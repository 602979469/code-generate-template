package ${basePackage}.core.repository.convertor;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.common.dal.query.${className}DalQuery;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
${convertorImports}

/**
 * ${entityName} DO/领域模型/查询参数互转，只存在于 repository。
 * 显式 get/set 赋值：DO 保持数据库原始类型，Model 按列级配置转换（枚举 / json / 强制类型）；
 * QueryParam（core-model）→ DalQuery（common-dal）在 Repository 调 Mapper 前完成，common-dal 不依赖 core-model。
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
        target.set${pkPropertyName?cap_first}(source.get${pkPropertyName?cap_first}());
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
        target.set${pkPropertyName?cap_first}(source.get${pkPropertyName?cap_first}());
<#list columns as c>
        target.set${c.propertyName?cap_first}(${c.toDoExpr?replace("{model}", "source")});
</#list>        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    /**
     * 查询参数 → common-dal 查询参数。
     *
     * @param source ${entityName}查询参数；为空返回空对象
     * @return ${entityName}查询参数（common-dal）
     */
    public static ${className}DalQuery toDalQuery(${className}QueryParam source) {
        ${className}DalQuery target = new ${className}DalQuery();
        if (source == null) {
            return target;
        }
        target.setPageNum(source.getPageNum());
        target.setPageSize(source.getPageSize());
<#list queryColumns as c>
        target.set${c.propertyName?cap_first}(${c.toDalExpr?replace("{query}", "source")});
</#list>        target.setCreateTimeBegin(source.getCreateTimeBegin());
        target.setCreateTimeEnd(source.getCreateTimeEnd());
        target.setUpdateTimeBegin(source.getUpdateTimeBegin());
        target.setUpdateTimeEnd(source.getUpdateTimeEnd());
        return target;
    }
}
