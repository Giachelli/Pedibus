package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Shift;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepo extends MongoRepository<Shift, ObjectId> {

    Shift getTurnByTurnID(ObjectId turnID);

}
