package ${pkgServiceImpl};

import ${basePackage}.common.framework.result.PageResult;
import ${pkgDomain}.${className};
import ${pkgParam}.${className}QueryParam;
import ${pkgRepository}.${className}Repository;
import ${pkgService}.${className}Service;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${entityName}领域服务实现：承载${entityName}相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class ${className}ServiceImpl implements ${className}Service {

    /** ${entityName}仓储。 */
    private final ${className}Repository ${classNameLower}Repository;

    public ${className}ServiceImpl(${className}Repository ${classNameLower}Repository) {
        this.${classNameLower}Repository = ${classNameLower}Repository;
    }

    @Override
    public ${className} create${className}(${className} ${classNameLower}) {
        return ${classNameLower}Repository.insert(${classNameLower});
    }

    @Override
    public int update${className}(${className} ${classNameLower}) {
        return ${classNameLower}Repository.update(${classNameLower});
    }

    @Override
    public int updateByCondition(${className} ${classNameLower}) {
        return ${classNameLower}Repository.updateByCondition(${classNameLower});
    }

    @Override
    public int delete${className}(${pkMethodArgs}) {
        return ${classNameLower}Repository.deleteBy${pkMethodName}(${pkCallArgs});
    }

    @Override
    public ${className} get${className}(${pkMethodArgs}) {
        return ${classNameLower}Repository.findBy${pkMethodName}(${pkCallArgs});
    }

    @Override
    public PageResult<${className}> findPage(${className}QueryParam query) {
        return ${classNameLower}Repository.findPage(query);
    }

    @Override
    public List<${className}> findList(${className}QueryParam query) {
        return ${classNameLower}Repository.findList(query);
    }
}
