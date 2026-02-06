package com.example.backend.dto.response;

        import com.example.backend.po.User;
        import lombok.Data;
        import lombok.NoArgsConstructor;

        import java.util.Map;

@Data
@NoArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private User user;
    private boolean requirePasswordChange; // 是否需要修改密码
    private Integer expiresIn; // token过期时间（秒）
    private Map<String, Object> additionalInfo; // 额外信息
}