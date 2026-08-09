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
    void createUser_duplicateUsername_throwsBizException() {
        when(userRepository.findByUsername("admin")).thenReturn(new User());

        assertThatThrownBy(() -> userDomainService.createUser(newUser(null, "admin")))
                .isInstanceOf(AiPlatformException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void createUser_success() {
        User user = newUser(null, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(userRepository.insert(user)).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User created = userDomainService.createUser(user);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getCreateTime()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(0);
        verify(userRepository).insert(user);
    }

    @Test
    void getUser_notFound_throwsBizException() {
        when(userRepository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userDomainService.getUser(999L))
                .isInstanceOf(AiPlatformException.class)
                .hasMessageContaining("用户不存在");
    }

    private User newUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        return user;
    }
}
