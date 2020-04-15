package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Message;
import ai.polito.lab2.demo.Repositories.MessageRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepo messageRepo;


    public ArrayList<Message> findMessagesByReceiverID(ObjectId receiverID){

        ArrayList<Message> messages = messageRepo.findAllByReceiverID(receiverID);
        return messages;

    }

    public Message findMessageByMessageID (ObjectId messageID){
        return messageRepo.findMessageByMessageID(messageID);
    }

    public void updateRead (Message message){
        messageRepo.save(message);
    }
    public void createMessageShift(ObjectId senderID, ObjectId receiverID, String action, long time){
        Message message = Message.builder()
                            .senderID(senderID)
                            .receiverID(receiverID)
                            .action(action)
                            .read(false)
                            .date(time)
                            .status("pending")
                            .build();

        messageRepo.save(message);

    }
}
