package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Turn;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnRepo extends MongoRepository<Turn, ObjectId> {

    Turn getTurnById(ObjectId turnID);
}
