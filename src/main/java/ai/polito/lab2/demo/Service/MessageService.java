package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Message;
import ai.polito.lab2.demo.viewmodels.MessageVM;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface MessageService {
    ArrayList<Message> findMessagesByReceiverID (ObjectId receiverID);
    void createMessageShift (ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId shiftID, String typeMessage);
    void createMessageResponse(ObjectId senderID, ArrayList<String> receivers, String action, long time, ObjectId shiftID, String status, String typeMessage);
    void createMessageResp(ObjectId senderID, ObjectId receiverID, ObjectId childID, String action, long time, String typeMessage);
    Message findMessageByMessageID (ObjectId messageID);
    Message findMessageByShiftID (ObjectId shiftID);
    void update(Message m);
    void createMessageNewUser(ObjectId senderID, ObjectId receiverID,String action, long time);
    void createMessageReservation(ObjectId senderID, ArrayList<String> receivers, String action, long time, ObjectId reservationID,String typeMessage);
    int deleteByMessageID(ObjectId messageID);
    Message findMessageByChildID(ObjectId childID);
    void createMessageNewRolesOtherAdmins(String sender, ArrayList<String> receivers, String action, long time, Integer routeID); //messaggio che ricevono tutti gli altri admin
    void createMessageNewRoles(String sender, ObjectId receiver, String action, long time, ArrayList<Integer> adminRoutes, ArrayList<Integer> muleRoutes); // messaggio che riceve lo user stesso
    void createMessageChildinPlace(String sender, String receiver, String action, long time, ObjectId childID, ObjectId reservationID);
    void createMessageEditAvailability(String senderID, ArrayList<String> receivers, String action, long time, Integer routeID);
    ArrayList<MessageVM> getMessages(String username);
    void readedUpdated(ObjectId messageID, Boolean read);
    Message editStatus(ObjectId messageID, String status);
    void createMessageDeleteTurns(ObjectId senderID, ArrayList<ObjectId> receiverID, String action, long time, ObjectId shiftID);

}
