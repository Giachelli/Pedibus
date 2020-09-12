package ai.polito.lab2.demo;

import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.viewmodels.UserVM;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Data
public class OnNewFileCompleteEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private UserVM user;

    public OnNewFileCompleteEvent(
            UserVM user, Locale locale, String appUrl) {
        super(user);

        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}
