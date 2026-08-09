<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${basePackage}.common.dal.mapper.${className}Mapper">

    <!-- 与 ${tableName} 表一一对应，id/create_time/update_time 由 BaseDO 承载；
         create_by/update_by/del_flag 等审计字段后续由 BizDO 启用，当前不映射 -->
    <sql id="selectColumns">
        ${selectColumns}
    </sql>

    <sql id="queryConditions">
        <where>
<#list queryColumns as c>
            <if test="${c.propertyName} != null<#if c.string> and ${c.propertyName} != ''</#if>">
                AND ${c.columnName} <#if c.queryType == "LIKE">LIKE CONCAT('%', #{${c.propertyName}}, '%')<#else>= #{${c.propertyName}}</#if>
            </if>
</#list>            <if test="createTimeBegin != null">
                AND create_time &gt;= #{createTimeBegin}
            </if>
            <if test="createTimeEnd != null">
                AND create_time &lt;= #{createTimeEnd}
            </if>
            <if test="updateTimeBegin != null">
                AND update_time &gt;= #{updateTimeBegin}
            </if>
            <if test="updateTimeEnd != null">
                AND update_time &lt;= #{updateTimeEnd}
            </if>
        </where>
    </sql>

    <select id="selectById" resultType="${basePackage}.common.dal.dataobject.${className}DO">
        SELECT <include refid="selectColumns"/>
        FROM ${tableName}
        WHERE id = #{id}
    </select>

    <select id="selectPage" resultType="${basePackage}.common.dal.dataobject.${className}DO">
        SELECT <include refid="selectColumns"/>
        FROM ${tableName}
        <include refid="queryConditions"/>
        ORDER BY id DESC
        LIMIT #{offset}, #{pageSize}
    </select>

    <select id="selectList" resultType="${basePackage}.common.dal.dataobject.${className}DO">
        SELECT <include refid="selectColumns"/>
        FROM ${tableName}
        <include refid="queryConditions"/>
        ORDER BY id DESC
    </select>

    <select id="countByQuery" resultType="long">
        SELECT COUNT(*)
        FROM ${tableName}
        <include refid="queryConditions"/>
    </select>

    <!-- create_time/update_time 由数据库自动维护（DEFAULT CURRENT_TIMESTAMP / ON UPDATE），不参与插入 -->
    <insert id="insert" parameterType="${basePackage}.common.dal.dataobject.${className}DO"
            useGeneratedKeys="true" keyProperty="id">
        INSERT INTO ${tableName} (${insertColumns})
        VALUES (${insertValues})
    </insert>

    <!-- 全量更新：覆盖所有业务字段；create_time/update_time 由数据库自动维护 -->
    <update id="update" parameterType="${basePackage}.common.dal.dataobject.${className}DO">
        UPDATE ${tableName}
        SET ${updateSet}
        WHERE id = #{id}
    </update>

    <!-- 按条件更新：只更新传入的非空字段（部分更新），适合只改几个字段的场景。
         全空守卫在 DomainService.updateByCondition（调用前判断，全空直接跳过）；
         XML 的 <if> 保证不会产生空 SET。直接调用 Mapper 时需自行保证至少一个非空字段。
         注意：无法把字段更新为 null，需要置 null 请用 update 全量更新；
         update_time 由数据库 ON UPDATE CURRENT_TIMESTAMP 自动维护 -->
    <update id="updateByCondition" parameterType="${basePackage}.common.dal.dataobject.${className}DO">
        <if test="<#list columns as c>${c.propertyName} != null<#sep> or </#sep></#list>">
            UPDATE ${tableName}
        <set>
<#list columns as c>
            <if test="${c.propertyName} != null">
                ${c.columnName} = #{${c.propertyName}},
            </if>
</#list>        </set>
            WHERE id = #{id}
        </if>
    </update>

    <!-- 当前为物理删除；软删除后续启用（del_flag 留待 BizDO） -->
    <delete id="deleteById">
        DELETE FROM ${tableName} WHERE id = #{id}
    </delete>
</mapper>
