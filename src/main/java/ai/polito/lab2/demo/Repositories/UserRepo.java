package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User, ObjectId> {

    User findByUsername(String username);

    User findUserBy_id(ObjectId objectId);

    ArrayList<User> findAll();

    User findByToken(String token);

    User findByPasstoken(String token);

}
