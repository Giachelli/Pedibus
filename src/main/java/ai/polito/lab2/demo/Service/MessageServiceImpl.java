package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Message;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.MessageRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ChildRepo childRepo;

    @Autowired
    private UserRepo userRepo;

    public ArrayList<Message> findMessagesByReceiverID(ObjectId receiverID){

        ArrayList<Message> messages = messageRepo.findAllByReceiverID(receiverID);
        return messages;

    }

    public Message findMessageByMessageID (ObjectId messageID){
        return messageRepo.findMessageByMessageID(messageID);
    }

    public void update (Message message){
        messageRepo.save(message);
    }

    public Message findMessageByChildID (ObjectId childID) {
        return messageRepo.findMessageByChildID(childID);
    }

    public void createMessageShift(ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId shiftID,String typeMessage){


        Message message = Message.builder()
                            .senderID(senderID)
                            .receiverID(receiverID)
                            .action(action)
                            .read(false)
                            .date(time)
                            .shiftID(shiftID)
                            .status("pending")
                            .build();

        messageRepo.save(message);

    }

    public void createMessageResponse(ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId shiftID,String status,String typeMessage){


        Message message = Message.builder()
                .senderID(senderID)
                .receiverID(receiverID)
                .action(action)
                .read(false)
                .date(time)
                .shiftID(shiftID)
                .status(status)
                .build();

        messageRepo.save(message);

    }

    public void createMessageResponse(ObjectId senderID, ObjectId receiverID, ObjectId childID, String action, long time,String typeMessage){
        Message message;

        Child child = childRepo.findChildByChildID(childID);

        if(typeMessage.equals("messageChildDelete")){
            Message m = findMessageByChildID(childID);
            messageRepo.deleteByMessageID(m.getMessageID());
            message = Message.builder()
                    .senderID(senderID)
                    .receiverID(receiverID)
                    .action(action)
                    .childID(childID)
                    .nameChild(child.getNameChild())
                    .familyName(child.getFamily_name())
                    .messageChildDelete(true)
                    .read(false)
                    .date(time)
                    .build();
        }else {
             message = Message.builder()
                    .senderID(senderID)
                    .receiverID(receiverID)
                    .action(action)
                    .childID(childID)
                    .nameChild(child.getNameChild())
                    .familyName(child.getFamily_name())
                    .messageChildCreation(true)
                    .read(false)
                    .date(time)
                    .build();

        }

        messageRepo.save(message);

    }

    public void createMessageReservation(ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId reservationID,String typeMessage){


        Message message = Message.builder()
                .senderID(senderID)
                .receiverID(receiverID)
                .action(action)
                .read(false)
                .date(time)
                .reservationID(reservationID)
                .build();

        messageRepo.save(message);

    }


    public void createMessageNewRolesOtherAdmins(String sender, ArrayList<String> receivers, String action, long time, Integer routeID) {
        ObjectId senderID = userRepo.findByUsername(sender).get_id();
        for (String s : receivers){
            ObjectId id = userRepo.findByUsername(s).get_id();

            Message message = Message.builder()
                    .senderID(senderID)
                    .receiverID(id)
                    .action(action)
                    .read(false)
                    .messageUpdateOtherUser(true)
                    .route(routeID)
                    .date(time)
                    .build();

            messageRepo.save(message);
        }
    }

    public void createMessageNewRoles(String sender, ObjectId receiver, String action, long time, ArrayList<Integer> adminRoutes, ArrayList<Integer> muleRoutes) {
        ObjectId senderID = userRepo.findByUsername(sender).get_id();

        Message message = Message.builder()
                    .senderID(senderID)
                    .receiverID(receiver)
                    .action(action)
                    .muleRoutes(muleRoutes)
                    .adminRoutes(adminRoutes)
                    .messageUpdateUser(true)
                    .read(false)
                    .date(time)
                    .build();

            messageRepo.save(message);
    }

    public Message findMessageByShiftID(ObjectId shiftID){
        return messageRepo.findMessageByShiftID(shiftID);
    }

    public void deleteByMessageID(ObjectId messageID){
        messageRepo.deleteByMessageID(messageID);
    }
}
