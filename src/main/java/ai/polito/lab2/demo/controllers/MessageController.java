package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Entity.*;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.viewmodels.MessageVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MessageController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private ReservationService reservationService;


    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<MessageVM>> getMessages(@RequestParam (required = true) String username){
        ObjectId receiverID= userService.getUserByUsername(username).get_id();
        ArrayList<Message> messages = messageService.findMessagesByReceiverID(receiverID);

        ArrayList<MessageVM> messageVMS = new ArrayList<>();

        for (Message message : messages)
        {
            String senderName = userService.getUserBy_id(message.getSenderID()).getUsername();
            //Get messaggio turni
            if(message.getShiftID()!=null){

                Shift shift = shiftService.getTurnByID(message.getShiftID());
                String pattern = "dd/MM";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(new Date(shift.getDate()));

                MessageVM messageVM = MessageVM.builder()
                        .sender(senderName)
                        .messageID(message.getMessageID().toString())
                        .text(message.getAction())
                        .read(message.getRead())
                        .date(message.getDate())
                        .shiftID(message.getShiftID().toString())
                        .status(message.getStatus())
                        .dateShift(date)
                        .direction(shift.isDirection())
                        .nameLinea(routeService.getRoutesByID(shift.getLineaID()).getNameR())
                        .build();
                messageVMS.add(messageVM);
            }else if(message.getReservationID()!=null){ //get messaggio che concerne le reservation
                Reservation reservation = reservationService.findReservationById(message.getReservationID());
                if(reservation != null){
                    String pattern = "dd/MM";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    String date = simpleDateFormat.format(new Date(reservation.getDate()));
                    MessageVM messageVM = MessageVM.builder()
                            .sender(senderName)
                            .messageID(message.getMessageID().toString())
                            .text(message.getAction())
                            .read(message.getRead())
                            .date(message.getDate())
                            .reservationID(message.getReservationID().toString())
                            .dateShift(date)
                            .directionReservation(reservation.getDirection())
                            .nameLinea(routeService.getRoutesByID(reservation.getRouteID()).getNameR())
                            .build();
                    messageVMS.add(messageVM);
                }
            }else{ //get messaggio che concerne il bimbo
                {
                    MessageVM messageVM = MessageVM.builder()
                            .sender(senderName)
                            .messageID(message.getMessageID().toString())
                            .text(message.getAction())
                            .read(message.getRead())
                            .date(message.getDate())
                            .build();
                    messageVMS.add(messageVM);
                };
            }
        }

        return ok().body(messageVMS);
    }

    @RequestMapping(value = "/messages/{messageID}/edit/{read}", method = RequestMethod.PUT)
    public ResponseEntity readedMessage(@PathVariable String messageID, @PathVariable Boolean read){
            Message message = messageService.findMessageByMessageID(new ObjectId(messageID));
            message.setRead(read);
            messageService.update(message);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @RequestMapping(value = "/messages/{messageID}/{status}", method = RequestMethod.PUT)
    public ResponseEntity editStatus(@PathVariable final String messageID, @PathVariable String status) {


        Message message = messageService.findMessageByMessageID(new ObjectId(messageID));

        //caso in cui il turno in questione è già stato modificato
        if(!message.getStatus().equals("pending")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (status.equals("accepted")|| status.equals("rejected")) {
            System.out.println("entrato!!!!!!!!!!!!!!!!!!!!!!!!! status è:" + status);
            message.setStatus(status);

            messageService.update(message);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
