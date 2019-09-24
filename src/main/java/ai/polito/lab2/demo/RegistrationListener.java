package ai.polito.lab2.demo;

import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.EmailServiceImpl;
import ai.polito.lab2.demo.Service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {
    private static final int EXPIRATION = 3; //60 * 24;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    EmailServiceImpl emailService;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        user.setExpiryDate(user.calculateExpiryDate(EXPIRATION));

        String recipientAddress = user.getUsername();
        String subject = "Registration Confirmation";
        String confirmationUrl = "/confirm/" + token;
        String message = "Questa è la mail di conferma della registrazione. Per attivare l'account cliccare il link di seguito";

        emailService.sendSimpleMessage(recipientAddress,subject, message + " " + "http://localhost:8080" + confirmationUrl);

        userRepo.save(user);
    }
}
