package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.viewmodels.ConfirmUserVM;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface IUserService {

    User getUserByUUID(String UUID);
    //void createPasswordResetTokenForUser(User user, String token);
    User getUserByPassUUID(String UUID);

    UserDTO getUserBy_id(ObjectId userID);

    void changePassword(User user, String password);

    boolean manageUser(String randomUUID, ConfirmUserVM userVM);

    UserDTO getUserByUsername(String name);

    ArrayList<UserDTO> findAll();
}
