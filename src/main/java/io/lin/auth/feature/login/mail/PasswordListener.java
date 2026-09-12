package io.lin.auth.feature.login.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class PasswordListener extends ApplicationEvent {
    private String email;
    private String username;
    private String password;
    public PasswordListener(String email, String username, String password) {
        super(email);
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
