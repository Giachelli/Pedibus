package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.User;

public interface IUserService {

    User getUserByUUID(String UUID);
    void createPasswordResetTokenForUser(User user, String token);
    User getUserByPassUUID(String UUID);
    void changePassword(User user, String password);
}
