package ${basePackage}.core.model.domain;

import lombok.Data;

/**
 * 示例扩展信息 POJO：jsonObject 绑定演示（generateExample 生成，业务可按需替换/删除）。
 */
@Data
public class Profile {

    /** 昵称。 */
    private String nickName;

    /** 邮箱。 */
    private String email;
}
