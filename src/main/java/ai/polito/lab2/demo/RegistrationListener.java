package ai.polito.lab2.demo;

import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.EmailServiceImpl;
import ai.polito.lab2.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {
    private static final int EXPIRATION = 30000; //60 * 24;

    @Autowired
    private UserService userService;

    @Autowired
    EmailServiceImpl emailService;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        UserDTO user = event.getUser();
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        user.setExpiryDate(user.calculateExpiryDate(EXPIRATION));

        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";
        String confirmationUrl = "/confirm/" + token;
        String message = "Questa è la mail di conferma della registrazione. Per attivare l'account cliccare il link di seguito";

        emailService.sendSimpleMessage(recipientAddress, subject, message + " " + "http://localhost:4200" + confirmationUrl);
        userService.saveUser(user);
    }
}
