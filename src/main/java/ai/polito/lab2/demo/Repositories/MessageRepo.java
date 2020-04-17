package ai.polito.lab2.demo.Repositories;


import ai.polito.lab2.demo.Entity.Message;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface MessageRepo extends MongoRepository<Message, ObjectId>{
    ArrayList<Message> findAllByReceiverID(ObjectId receiverID);
    Message findMessageByMessageID(ObjectId messageID);
    Message findMessageByShiftID(ObjectId messageID);
}

