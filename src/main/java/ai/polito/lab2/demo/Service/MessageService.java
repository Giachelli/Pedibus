package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Message;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface MessageService {
    ArrayList<Message> findMessagesByReceiverID (ObjectId receiverID);
    void createMessageShift (ObjectId senderID, ObjectId receiverID, String action, long time);
    Message findMessageByMessageID (ObjectId messageID);
    void updateRead(Message m);
}
