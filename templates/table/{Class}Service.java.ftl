package ${pkgService};

import ${basePackage}.common.util.result.PageResult;
import ${pkgDomain}.${className};
import ${pkgParam}.${className}QueryParam;

import java.util.List;

/**
 * ${entityName}领域服务
 *
 * 实现类为 ${className}ServiceImpl（core.service.impl 包）。
 */
public interface ${className}Service {

    /**
     * 创建${entityName}
     *
     * @param ${classNameLower} ${entityName}
     * @return 创建后的${entityName}（主键已回填）
     */
    ${className} create${className}(${className} ${classNameLower});

    /**
     * 更新${entityName}（全量）
     *
     * @param ${classNameLower} ${entityName}（含主键）
     * @return 受影响行数；0 表示未生效，由上层决定
     */
    int update${className}(${className} ${classNameLower});

    /**
     * 按条件更新${entityName}（只更新传入的非空字段）。
     *
     * @param ${classNameLower} ${entityName}（至少含主键）
     * @return 受影响行数；0 表示未生效，由上层决定
     */
    int updateByCondition(${className} ${classNameLower});

    /**
     * 删除${entityName}
     *
     * @param ${pkCallArgs} ${entityName}主键
     * @return 受影响行数；0 表示未生效，由上层决定
     */
    int delete${className}(${pkMethodArgs});

    /**
     * 按主键获取${entityName}
     *
     * @param ${pkCallArgs} ${entityName}主键
     * @return ${entityName}
     */
    ${className} get${className}(${pkMethodArgs});

    /**
     * 分页查询${entityName}
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<${className}> findPage(${className}QueryParam query);

    /**
     * 列表查询${entityName}
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    List<${className}> findList(${className}QueryParam query);
}
