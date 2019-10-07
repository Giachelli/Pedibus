package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Turn;
import org.bson.types.ObjectId;

import java.util.Optional;

public interface TurnService {
    void save(Turn t);
    Turn getTurnByID(ObjectId turnID);
}
