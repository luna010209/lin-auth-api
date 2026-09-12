package io.lin.auth.feature.auth.service.sendMail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class EmailVerifyListener extends ApplicationEvent {
    private String email;
    private Integer code;
    public EmailVerifyListener(String email, Integer code) {
        super(email);
        this.email = email;
        this.code= code;
    }
}
