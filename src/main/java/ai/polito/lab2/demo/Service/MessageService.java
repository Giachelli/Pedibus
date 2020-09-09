package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Message;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface MessageService {
    ArrayList<Message> findMessagesByReceiverID (ObjectId receiverID);
    void createMessageShift (ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId shiftID, String typeMessage);
    void createMessageResponse(ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId shiftID, String status, String typeMessage);
    void createMessageResponse(ObjectId senderID, ObjectId receiverID, ObjectId childID, String action, long time, String typeMessage);
    Message findMessageByMessageID (ObjectId messageID);
    void update(Message m);
    Message findMessageByShiftID(ObjectId shiftID);
    void createMessageReservation(ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId reservationID,String typeMessage);
    void deleteByMessageID(ObjectId messageID);
    Message findMessageByChildID(ObjectId childID);
    void createMessageNewRolesOtherAdmins(String sender, ArrayList<String> receivers, String action, long time, Integer routeID); //messaggio che ricevono tutti gli altri admin
    void createMessageNewRoles(String sender, ObjectId receiver, String action, long time, ArrayList<Integer> adminRoutes, ArrayList<Integer> muleRoutes); // messaggio che riceve lo user stesso
}
