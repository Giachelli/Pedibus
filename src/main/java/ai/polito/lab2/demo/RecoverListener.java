package ai.polito.lab2.demo;

import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Service.EmailServiceImpl;
import ai.polito.lab2.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RecoverListener implements
        ApplicationListener<OnRecoverCompleteEvent> {
    private static final int EXPIRATION = 30000; //60 * 24;

    @Autowired
    private UserService userService;

    @Autowired
    EmailServiceImpl emailService;

    @Override
    public  void onApplicationEvent(OnRecoverCompleteEvent event) {
        this.confirmRecover(event);
    }

    private void confirmRecover(OnRecoverCompleteEvent event) {
        User user = event.getUser();
        String passtoken = UUID.randomUUID().toString();
        user.setPasstoken(passtoken);

        user.setExpiry_passToken(user.calculateExpiryDate(EXPIRATION));

        String recipientAddress = user.getUsername();
        String subject = "Recupero Password";
        String confirmationUrl = "/recover/" + passtoken;
        String message = "Stai recuperando la password. Per continuare, clicca il link di seguito";

        emailService.sendSimpleMessage(recipientAddress, subject, message + " " + "http://localhost:4200" + confirmationUrl);
        userService.saveUser(user);
    }
}
