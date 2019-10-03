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

    UserDTO getUserDTOBy_id(ObjectId userID);

    User getUserBy_id(ObjectId userID);

    void changePassword(User user, String password);

    boolean userEnabled(String username);

    void saveUser(User u);

    boolean manageUser(String randomUUID, ConfirmUserVM userVM);

    UserDTO getUserDTOByUsername(String name);

    ArrayList<UserDTO> findAll();

    boolean deleteUserbyID(ObjectId userID);

    User getUserByUsername(String name);
}
