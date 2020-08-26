package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Stop;
import org.bson.types.ObjectId;

public interface StopService {
    Stop findStopbyId(ObjectId id);
    Stop findStopbyNameAndNumS(String name, int nums);
}
