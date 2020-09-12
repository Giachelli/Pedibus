package ai.polito.lab2.demo;

import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Service.EmailServiceImpl;
import ai.polito.lab2.demo.Service.UserService;
import ai.polito.lab2.demo.viewmodels.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Component
public class NewLineListener implements
        ApplicationListener<OnNewFileCompleteEvent> {


    @Autowired
    EmailServiceImpl emailService;

    @Override
    public  void onApplicationEvent(OnNewFileCompleteEvent event) {
        this.confirmNewLine(event);
    }

    private void confirmNewLine(OnNewFileCompleteEvent event) {
        UserVM user = event.getUser();

        String recipientAddress = user.getUsername();
        String subject = "E' stata aggiunta una nuova linea";
        String message = "E' stata aggiunta una nuova linea in Pedibus per cui sei dichiarato amministratore.";

        emailService.sendSimpleMessage(recipientAddress, subject, message);
    }
}
