package ${basePackage}.core.service;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.exception.AiPlatformException;
import ${basePackage}.core.repository.${className}Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ${className}领域服务单元测试：Mock 仓储，验证领域规则。
 */
@ExtendWith(MockitoExtension.class)
class ${className}DomainServiceTest {

    @Mock
    private ${className}Repository ${classNameLower}Repository;

    @InjectMocks
    private ${className}DomainService ${classNameLower}DomainService;

<#if requiredColumns?has_content>
    @Test
    void create${className}_missingRequired_throwsBizException() {
        ${className} ${classNameLower} = new ${className}();

        assertThatThrownBy(() -> ${classNameLower}DomainService.create${className}(${classNameLower}))
                .isInstanceOf(AiPlatformException.class);
    }

    @Test
    void create${className}_success() {
        ${className} ${classNameLower} = new ${className}();
<#list requiredColumns as c>
        ${classNameLower}.set${c.propertyName?cap_first}(<#if c.javaType == "String">"test"<#elseif c.javaType == "Long">1L<#elseif c.javaType == "Integer">1<#elseif c.javaType == "LocalDateTime">LocalDateTime.now()<#elseif c.javaType == "LocalDate">LocalDate.now()<#elseif c.javaType == "BigDecimal">BigDecimal.ONE<#elseif c.javaType == "Boolean">true<#elseif c.javaType == "Double">1.0<#else>null</#if>);
</#list>        when(${classNameLower}Repository.insert(${classNameLower})).thenAnswer(invocation -> {
            ${className} saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ${className} created = ${classNameLower}DomainService.create${className}(${classNameLower});

        assertThat(created.getId()).isEqualTo(1L);
        verify(${classNameLower}Repository).insert(${classNameLower});
    }

</#if>
    @Test
    void get${className}_notFound_throwsBizException() {
        when(${classNameLower}Repository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> ${classNameLower}DomainService.get${className}(999L))
                .isInstanceOf(AiPlatformException.class)
                .hasMessageContaining("资源不存在");
    }

    @Test
    void delete${className}_notFound_throwsBizException() {
        when(${classNameLower}Repository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> ${classNameLower}DomainService.delete${className}(999L))
                .isInstanceOf(AiPlatformException.class)
                .hasMessageContaining("资源不存在");
    }
}
