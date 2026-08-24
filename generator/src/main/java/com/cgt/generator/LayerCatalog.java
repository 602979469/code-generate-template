package com.cgt.generator;

import java.util.List;

/** 基线层目录表（aiplatform 真实文件集，见《03-代码生成规范》）。 */
public final class LayerCatalog {

    /** 全部层（含资源文件），按生成顺序排列。 */
    public static final List<LayerSpec> ALL = List.of(
            new LayerSpec("common-dal-do", "common/dal", "common.dal", "dataobject",
                    List.of("{Model}DO.java"), true, false, false),
            new LayerSpec("common-dal-mapper", "common/dal", "common.dal", "mapper",
                    List.of("{Model}Mapper.java"), true, false, false),
            new LayerSpec("common-dal-mapper-xml", "common/dal", "common.dal", "mapper",
                    List.of("{Model}Mapper.xml"), true, false, true),
            new LayerSpec("common-dal-query", "common/dal", "common.dal", "query",
                    List.of("{Model}DalQuery.java"), true, false, false),
            new LayerSpec("core-model-domain", "core/model", "core.model", "domain",
                    List.of("{Model}.java"), false, false, false),
            new LayerSpec("core-model-param", "core/model", "core.model", "param",
                    List.of("{Model}QueryParam.java"), false, false, false),
            new LayerSpec("core-repository", "core/repository", "core.repository", "repository",
                    List.of("{Model}Repository.java"), false, false, false),
            new LayerSpec("core-repository-impl", "core/repository", "core.repository", "repository/impl",
                    List.of("{Model}RepositoryImpl.java"), false, false, false),
            new LayerSpec("core-repository-convertor", "core/repository", "core.repository", "repository/convertor",
                    List.of("{Model}Convertor.java"), false, false, false),
            new LayerSpec("core-service", "core/service", "core.service", "service",
                    List.of("{Model}Service.java"), false, false, false),
            new LayerSpec("core-service-impl", "core/service", "core.service", "service/impl",
                    List.of("{Model}ServiceImpl.java"), false, false, false),
            new LayerSpec("biz-manager", "biz/service-impl", "biz.service", "service",
                    List.of("{Model}Manager.java"), false, true, false),
            new LayerSpec("biz-manager-impl", "biz/service-impl", "biz.service", "service/impl",
                    List.of("{Model}ManagerImpl.java"), false, true, false),
            new LayerSpec("web-controller", "web", "web", "controller",
                    List.of("{Model}Controller.java"), false, true, false),
            new LayerSpec("web-checker", "web", "web", "checker",
                    List.of("{Model}ParamChecker.java"), false, true, false),
            new LayerSpec("web-param", "web", "web", "param",
                    List.of("{Model}CreateRequest.java", "{Model}UpdateRequest.java", "{Model}QueryRequest.java"),
                    false, true, false),
            new LayerSpec("web-result", "web", "web", "result",
                    List.of("{Model}Response.java"), false, true, false),
            new LayerSpec("web-assembler", "web", "web", "assembler",
                    List.of("{Model}Assembler.java"), false, true, false)
    );

    private LayerCatalog() {
    }
}
