package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Child;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildRepo extends MongoRepository<Child, ObjectId> {
    Child findChildByNameChild(String nameChild);
    Child findChildByIdChild (ObjectId childId);
}
