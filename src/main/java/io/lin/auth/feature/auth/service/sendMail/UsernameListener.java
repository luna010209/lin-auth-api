package io.lin.auth.feature.auth.service.sendMail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class UsernameListener extends ApplicationEvent {
    private String email;
    private String username;
    public UsernameListener(String email, String username) {
        super(email);
        this.email = email;
        this.username = username;
    }
}
