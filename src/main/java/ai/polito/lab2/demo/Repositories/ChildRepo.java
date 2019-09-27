package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Child;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ChildRepo extends MongoRepository<Child, ObjectId> {
    Child findChildByNameChild(String nameChild);

    Child findChildByNameChildAndIdFamily(String nameChild, ObjectId idFamily);

    Child findChildByIdChild(ObjectId childId);

    ArrayList<Child> findChildByIdFamily(ObjectId familyId);
}
