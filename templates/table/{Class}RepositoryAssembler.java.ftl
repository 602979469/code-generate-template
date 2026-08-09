package ${basePackage}.core.repository.assembler;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.core.model.domain.${className};
import org.springframework.beans.BeanUtils;

/**
 * ${tableComment} DO 与领域模型互转，只存在于 repository。
 */
public final class ${className}Assembler {

    private ${className}Assembler() {
    }

    /**
     * DO → 领域模型。
     *
     * @param ${classNameLower}DO ${tableComment}数据对象；为空返回 null
     * @return ${entityName}领域模型
     */
    public static ${className} toModel(${className}DO ${classNameLower}DO) {
        if (${classNameLower}DO == null) {
            return null;
        }
        ${className} ${classNameLower} = new ${className}();
        BeanUtils.copyProperties(${classNameLower}DO, ${classNameLower});
        return ${classNameLower};
    }

    /**
     * 领域模型 → DO。
     *
     * @param ${classNameLower} ${entityName}领域模型
     * @return ${tableComment}数据对象
     */
    public static ${className}DO toDO(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = new ${className}DO();
        BeanUtils.copyProperties(${classNameLower}, ${classNameLower}DO);
        return ${classNameLower}DO;
    }
}
