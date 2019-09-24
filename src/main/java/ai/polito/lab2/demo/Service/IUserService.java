package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.viewmodels.ConfirmUserVM;

public interface IUserService {

    User getUserByUUID(String UUID);
    //void createPasswordResetTokenForUser(User user, String token);
    User getUserByPassUUID(String UUID);
    void changePassword(User user, String password);

    boolean manageUser(String randomUUID, ConfirmUserVM userVM);
}
