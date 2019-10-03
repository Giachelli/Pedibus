package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Stop;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StopRepo extends MongoRepository<Stop, Integer> {
    Stop findStopBy_id(ObjectId id);

    Stop findStopByNome(String nome);
}
