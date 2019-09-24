package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private static final int EXPIRATION = 3;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailServiceImpl emailService;

    @Override
    public User getUserByUUID(String UUID) {
        return userRepo.findByToken(UUID);
    }

    @Override
    public User getUserByPassUUID(String UUID) {
        return userRepo.findByPasstoken(UUID);
    }

    @Override
    public void changePassword(User user, String password){

        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }

    public void createPasswordResetTokenForUser(User user, String token) {

        /*if(user.getPass_token()==null || user.getPass_token().isEmpty()){
            user.setPass_token(Arrays.asList(token));
        }else{
            user.getPass_token().add(token);
        }*/
        user.setPasstoken(token);
        user.setExpiry_passToken(user.calculateExpiryDate(EXPIRATION));

        String recipientAddress = user.getUsername();
        String subject = "Request Change Password";
        String confirmationUrl = "/recover/" + token;
        String message = "Questa mail serve per cambiare password. Clicca sul token";

        emailService.sendSimpleMessage(recipientAddress,subject, message + " " + "http://localhost:8080" + confirmationUrl); //non è troppo scalabile, vedere meglio come si fa
        userRepo.save(user);
    }


}