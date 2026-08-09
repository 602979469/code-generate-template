package ${basePackage}.core.repository.convertor;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.core.model.domain.${className};
import org.springframework.beans.BeanUtils;

/**
 * ${tableComment} DO 与 Model 互转。
 */
public class ${className}Convertor {

    public static ${className} toModel(${className}DO ${classNameLower}DO) {
        if (${classNameLower}DO == null) {
            return null;
        }
        ${className} ${classNameLower} = new ${className}();
        BeanUtils.copyProperties(${classNameLower}DO, ${classNameLower});
        return ${classNameLower};
    }

    public static ${className}DO toDO(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = new ${className}DO();
        BeanUtils.copyProperties(${classNameLower}, ${classNameLower}DO);
        return ${classNameLower}DO;
    }
}
