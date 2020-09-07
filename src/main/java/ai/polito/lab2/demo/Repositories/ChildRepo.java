package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Child;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ChildRepo extends MongoRepository<Child, ObjectId> {
    Child findChildByNameChild(String nameChild);

    Child findChildByChildID(ObjectId childId);

    ArrayList<Child> findAll();

    Child findChildByChildID(String childId);

    ArrayList<Child> findChildByUsername(String Username);

    Child findChildByNameChildAndUsername(String nameChild, String Username);
}
