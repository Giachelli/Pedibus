package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Entity.Message;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.MessageService;
import ai.polito.lab2.demo.Service.UserService;
import ai.polito.lab2.demo.viewmodels.MessageVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MessageController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;



    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<MessageVM>> getMessages(@RequestParam (required = true) String username){
        ObjectId receiverID= userService.getUserByUsername(username).get_id();
        ArrayList<Message> messages = messageService.findMessagesByReceiverID(receiverID);

        ArrayList<MessageVM> messageVMS = new ArrayList<>();

        for (Message message : messages)
        {
            String senderName = userService.getUserBy_id(message.getSenderID()).getUsername();

            MessageVM messageVM = MessageVM.builder()
                                  .sender(senderName)
                                  .text(message.getAction())
                                  .read(message.getRead())
                                  .date(message.getDate())
                                  .status(message.getStatus())
                                  .build();
            messageVMS.add(messageVM);
        }

        return ok().body(messageVMS);
    }

    @RequestMapping(value = "/messages/{messageID}", method = RequestMethod.PUT)
    public ResponseEntity readedMessage(@PathVariable String messageID){
            Message message = messageService.findMessageByMessageID(new ObjectId(messageID));
            message.setRead(true);
            messageService.updateRead(message);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
