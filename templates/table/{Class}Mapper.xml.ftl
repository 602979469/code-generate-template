<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${basePackage}.common.dal.mapper.${className}Mapper">

    <sql id="selectColumns">
        ${selectColumns}
    </sql>

    <sql id="queryConditions">
        <where>
<#list queryColumns as c>
            <if test="${c.propertyName} != null<#if c.string> and ${c.propertyName} != ''</#if>">
                AND ${c.columnName} <#if c.queryType == "LIKE">LIKE CONCAT('%', #{${c.propertyName}}, '%')<#else>= #{${c.propertyName}}</#if>
            </if>
</#list><#if logicDeleteEnabled>
            AND ${logicDeleteColumn} = ${logicDeleteNormal}
</#if>            <if test="createTimeBegin != null">
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

    <select id="selectBy${pkMethodName}" resultType="${basePackage}.common.dal.dataobject.${className}DO">
        SELECT <include refid="selectColumns"/>
        FROM ${tableName}
        WHERE ${pkWhere}
<#if logicDeleteEnabled>
        AND ${logicDeleteColumn} = ${logicDeleteNormal}
</#if>
    </select>

    <select id="selectPage" resultType="${basePackage}.common.dal.dataobject.${className}DO">
        SELECT <include refid="selectColumns"/>
        FROM ${tableName}
        <include refid="queryConditions"/>
        ORDER BY ${pkOrderBy}
        LIMIT #{offset}, #{pageSize}
    </select>

    <select id="selectList" resultType="${basePackage}.common.dal.dataobject.${className}DO">
        SELECT <include refid="selectColumns"/>
        FROM ${tableName}
        <include refid="queryConditions"/>
        ORDER BY ${pkOrderBy}
    </select>

    <select id="selectOne" resultType="${basePackage}.common.dal.dataobject.${className}DO">
        SELECT <include refid="selectColumns"/>
        FROM ${tableName}
        <include refid="queryConditions"/>
        ORDER BY ${pkOrderBy}
    </select>

    <select id="countByQuery" resultType="long">
        SELECT COUNT(*)
        FROM ${tableName}
        <include refid="queryConditions"/>
    </select>

    <insert id="insert" parameterType="${basePackage}.common.dal.dataobject.${className}DO"<#if pkAuto>
            useGeneratedKeys="true" keyProperty="${pkPropertyName}"</#if>>
        INSERT INTO ${tableName} (${insertColumns})
        VALUES (${insertValues})
    </insert>

    <update id="update" parameterType="${basePackage}.common.dal.dataobject.${className}DO">
        UPDATE ${tableName}
        SET ${updateSet}
        WHERE ${pkWhere}
<#if logicDeleteEnabled>
        AND ${logicDeleteColumn} = ${logicDeleteNormal}
</#if>
    </update>

    <update id="updateByCondition" parameterType="${basePackage}.common.dal.dataobject.${className}DO">
        <if test="<#list columns as c>${c.propertyName} != null<#sep> or </#sep></#list>">
            UPDATE ${tableName}
        <set>
<#list columns as c>
            <if test="${c.propertyName} != null">
                ${c.columnName} = #{${c.propertyName}},
            </if>
</#list><#if !updateTimeAuto>            update_time = NOW(),
</#if>        </set>
            WHERE ${pkWhere}
<#if logicDeleteEnabled>
            AND ${logicDeleteColumn} = ${logicDeleteNormal}
</#if>
        </if>
    </update>

<#if logicDeleteEnabled>
    <update id="deleteBy${pkMethodName}" parameterType="${basePackage}.common.dal.dataobject.${className}DO">
        UPDATE ${tableName}
        SET ${logicDeleteColumn} = ${logicDeleteDelete}
        WHERE ${pkWhere} AND ${logicDeleteColumn} = ${logicDeleteNormal}
    </update>
<#else>
    <delete id="deleteBy${pkMethodName}">
        DELETE FROM ${tableName} WHERE ${pkWhere}
    </delete>
</#if>
</mapper>
