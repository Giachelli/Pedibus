package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Role;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends MongoRepository<Role, ObjectId> {

    Role findByRole(String role);
}