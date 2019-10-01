package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.viewmodels.ConfirmUserVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

@Service
public class UserService implements IUserService {

    private static final int EXPIRATION = 3;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailServiceImpl emailService;

    public void saveUser(UserDTO userDTO){

        User user = User.builder().
                    username(userDTO.getEmail()).
                    roles(userDTO.getRoles()).
                    token(userDTO.getToken()).
                    expiryDate(userDTO.getExpiryDate()).
                    isEnabled(false).build();

        User u = userRepo.findByUsername(user.getUsername());
        if (u != null) /// da vedere cosa ritorna
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already registered"); //vabene così tanto non ci ha chiesto di restituirgli qualcosa, l'importante è che non si registri

        this.userRepo.save(user);

    }

    @Override
    public User getUserByUUID(String UUID) {

        return userRepo.findByToken(UUID);
    }

    @Override
    public User getUserByPassUUID(String UUID) {
        return userRepo.findByPasstoken(UUID);
    }

    @Override
    public UserDTO getUserBy_id(ObjectId userID) {
        User u = userRepo.findUserBy_id(userID);
        UserDTO userDTO = u.convertToDTO();
        return userDTO;
    }

    @Override
    public void changePassword(User user, String password){

        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }

    @Override
    public boolean manageUser(String randomUUID, ConfirmUserVM userVM) {
        User user = this.getUserByUUID(randomUUID);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: User not found");
        }
        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already active");
        }
        Calendar cal = Calendar.getInstance();
        // in questo caso l'admin deve mandare di nuovo una mail perchè è scaduto il token
        if ((user.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired token");
        }

        user.setFamily_name(userVM.getFamily_name());
        user.setPassword(userVM.getPassword());
        user.setEnabled(true);
        userRepo.save(user);
        return true;
    }

    @Override
    public UserDTO getUserByUsername(String name) {
        User u = userRepo.findUserByUsername(name);
        UserDTO userDTO = u.convertToDTO();
        return userDTO;
    }

    @Override
    public ArrayList<UserDTO> findAll() {
        ArrayList<User> users = userRepo.findAll();
        ArrayList<UserDTO> userDTOArrayList = new ArrayList<>();
        for(User user : users)
        {
            UserDTO userDTO = user.convertToDTO();
            userDTOArrayList.add(userDTO);
        }
        return userDTOArrayList;
    }

    /*public void createPasswordResetTokenForUser(User user, String token) {

        user.setPasstoken(token);
        user.setExpiry_passToken(user.calculateExpiryDate(EXPIRATION));

        String recipientAddress = user.getUsername();
        String subject = "Request Change Password";
        String confirmationUrl = "/recover/" + token;
        String message = "Questa mail serve per cambiare password. Clicca sul token";

        emailService.sendSimpleMessage(recipientAddress,subject, message + " " + "http://localhost:8080" + confirmationUrl); //non è troppo scalabile, vedere meglio come si fa
        userRepo.save(user);
    }*/


}