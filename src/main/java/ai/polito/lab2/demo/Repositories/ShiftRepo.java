package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Shift;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepo extends MongoRepository<Shift, ObjectId> {

    Shift getTurnByTurnID(ObjectId turnID);

    List<Shift> findByLineaIDAndMuleID(int routeID, ObjectId muleID);

    List<Shift> findByLineaIDAndMuleIDAndDateAfter(int routeID, ObjectId muleID,long dateAfter);
}
