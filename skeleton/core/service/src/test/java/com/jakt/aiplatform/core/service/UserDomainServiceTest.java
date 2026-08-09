package com.jakt.aiplatform.core.service;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.exception.AiPlatformException;
import com.jakt.aiplatform.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户领域服务单元测试：Mock 仓储，验证领域规则。
 */
@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDomainService userDomainService;

    @Test
    void createUser_blankLoginName_throwsBizException() {
        User user = new User();
        user.setLoginName("  ");

        assertThatThrownBy(() -> userDomainService.createUser(user))
                .isInstanceOf(AiPlatformException.class)
                .hasMessageContaining("登录账号不能为空");
    }

    @Test
    void createUser_success() {
        User user = new User();
        user.setLoginName("admin");
        when(userRepository.insert(user)).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User created = userDomainService.createUser(user);

        assertThat(created.getId()).isEqualTo(1L);
        verify(userRepository).insert(user);
    }

    @Test
    void getUser_notFound_throwsBizException() {
        when(userRepository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userDomainService.getUser(999L))
                .isInstanceOf(AiPlatformException.class)
                .hasMessageContaining("资源不存在");
    }

    @Test
    void deleteUser_notFound_throwsBizException() {
        when(userRepository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userDomainService.deleteUser(999L))
                .isInstanceOf(AiPlatformException.class)
                .hasMessageContaining("资源不存在");
    }
}
