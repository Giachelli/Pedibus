package ai.polito.lab2.demo;

import ai.polito.lab2.demo.Dto.UserDTO;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Data
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private UserDTO user;

    public OnRegistrationCompleteEvent(
            UserDTO user, Locale locale, String appUrl) {
        super(user);

        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}

