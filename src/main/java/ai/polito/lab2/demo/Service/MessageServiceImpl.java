package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.*;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.MessageRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.viewmodels.MessageVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ChildRepo childRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private StopService stopService;

    @Autowired
    private ChildService childService;

    @Autowired
    private ReservationService reservationService;

    public ArrayList<Message> findMessagesByReceiverID(ObjectId receiverID) {

        ArrayList<Message> messages = messageRepo.findAllByReceiverID(receiverID);
        return messages;

    }

    public Message findMessageByMessageID(ObjectId messageID) {
        return messageRepo.findMessageByMessageID(messageID);
    }

    public void update(Message message) {
        messageRepo.save(message);
    }

    public Message findMessageByChildID(ObjectId childID) {
        return messageRepo.findMessageByChildID(childID);
    }

    public void createMessageShift(ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId shiftID, String typeMessage) {


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

    public void createMessageResponse(ObjectId senderID, ArrayList<String> receivers, String action, long time, ObjectId shiftID, String status, String typeMessage) {

        for (String s : receivers) {
            ObjectId receiverID = userRepo.findByUsername(s).get_id();

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
    }

    public void createMessageResp(ObjectId senderID, ObjectId receiverID, ObjectId childID, String action, long time, String typeMessage) {
        Message message;

        Child child = childRepo.findChildByChildID(childID);

        if (typeMessage.equals("messageChildDelete")) {
            Message m = findMessageByChildID(childID);
            if (m != null)
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
        } else {
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

    public void createMessageReservation(ObjectId senderID, ArrayList<String> receivers, String action, long time, ObjectId reservationID, String typeMessage) {

        for (String s : receivers) {
            ObjectId receiverID = userRepo.findByUsername(s).get_id();

            Message message = Message.builder()
                    .senderID(senderID)
                    .receiverID(receiverID)
                    .action(action)
                    .read(false)
                    .date(time)
                    .reservationID(reservationID)
                    .messageChildPrenotation(true)
                    .build();

            messageRepo.save(message);
        }
    }


    public void createMessageNewRolesOtherAdmins(String sender, ArrayList<String> receivers, String action, long time, Integer routeID) {
        ObjectId senderID = userRepo.findByUsername(sender).get_id();
        for (String s : receivers) {
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

    public void createMessageEditAvailability(String sender, ArrayList<String> receivers, String action, long time, Integer routeID) {
        ObjectId senderID = userRepo.findByUsername(sender).get_id();
        for (String s : receivers) {
            ObjectId id = userRepo.findByUsername(s).get_id();

            Message message = Message.builder()
                    .senderID(senderID)
                    .receiverID(id)
                    .action(action)
                    .read(false)
                    .messageEditAvailability(true)
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

    public void createMessageNewUser(ObjectId senderID, ObjectId receiverID, String action, long time) {
        Message message = Message.builder()
                .senderID(senderID)
                .receiverID(receiverID)
                .userID(senderID)
                .action(action)
                .read(false)
                .date(time)
                .messageNewUser(true)
                .build();

        messageRepo.save(message);

    }

    public void createMessageChildinPlace(String sender, String receiver, String action, long time, ObjectId childID, ObjectId reservationID) {
        ObjectId senderID = userRepo.findByUsername(sender).get_id();
        ObjectId receiverID = userRepo.findByUsername(receiver).get_id();


        Message message = Message.builder()
                .senderID(senderID)
                .receiverID(receiverID)
                .reservationID(reservationID)
                .childID(childID)
                .action(action)
                .messageChildPlace(true)
                .read(false)
                .date(time)
                .build();

        messageRepo.save(message);


    }

    public Message findMessageByShiftID(ObjectId shiftID) {
        return messageRepo.findMessageByShiftID(shiftID);
    }

    public void deleteByMessageID(ObjectId messageID) {
        messageRepo.deleteByMessageID(messageID);
    }

    public ArrayList<MessageVM> getMessages(String username) {
        ObjectId receiverID = userService.getUserByUsername(username).get_id();
        ArrayList<Message> messages = findMessagesByReceiverID(receiverID);
        ArrayList<MessageVM> messageVMS = new ArrayList<>();

        for (Message message : messages) {
            String senderName = userService.getUserBy_id(message.getSenderID()).getUsername();
            //Get messaggio turni
            if (message.getShiftID() != null) {
                Shift shift = shiftService.getTurnByID(message.getShiftID());
                if ( shift != null){
                    String pattern = "dd/MM";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    String date = simpleDateFormat.format(new Date(shift.getDate()));
                    MessageVM messageVM = MessageVM.builder()
                            .sender(senderName)
                            .messageID(message.getMessageID().toString())
                            .text(message.getAction())
                            .read(message.getRead())
                            .date(message.getDate())
                            .nameStop(stopService.findStopbyId(shift.getStartID()).getNome())
                            .oraFermata(stopService.findStopbyId(shift.getStartID()).getTime())
                            .nameStopDiscesa(stopService.findStopbyId(shift.getStopID()).getNome())
                            .oraFermataDiscesa(stopService.findStopbyId(shift.getStopID()).getTime())
                            .shiftID(message.getShiftID().toString())
                            .messageShiftRequest(true) //TODO: dopo aver parlato con gli altri
                            .status(message.getStatus())
                            .dateShift(date)
                            .direction(shift.isDirection())
                            .nameLinea(routeService.getRoutesByID(shift.getLineaID()).getNameR())
                            .build();
                    messageVMS.add(messageVM);
                }
                /* get messaggio che concerne le reservation. Due tipi di messaggi per tre azioni differenti:
                prenotazione bimbo da calendario, bimbo prenotato preso in carica,
                bimbo non prenotato preso in carica
                 */
            } else if (message.getMessageNewUser() != null) {
                User u = userService.getUserBy_id(message.getUserID());
                if (u != null) { // se fosse stato cancellato potrebbe esser null
                    MessageVM messageVM = MessageVM.builder()
                            .sender(senderName)
                            .messageID(message.getMessageID().toString())
                            .text(message.getAction())
                            .read(message.getRead())
                            .date(message.getDate())
                            .messageNewUser(true)
                            .familyName(u.getFamily_name())
                            .username(u.getUsername())
                            .build();
                    messageVMS.add(messageVM);
                }
            } else if (message.getReservationID() != null) {
                if (message.getMessageChildPlace() != null) {
                    Reservation r = reservationService.findReservationById(message.getReservationID());
                    if (r != null) {
                        Boolean direction = ((r.getDirection().equals("andata")) ? true : false);
                        System.out.println("stopService.findStopbyId(r.getStopID()).getNome()!!!" + stopService.findStopbyId(r.getStopID()).getNome());
                        MessageVM messageVM = MessageVM.builder()
                                .sender(senderName)
                                .messageID(message.getMessageID().toString())
                                .text(message.getAction())
                                .read(message.getRead())
                                .date(message.getDate())
                                .messageChildPlace(true)
                                .nameChild(childService.findChildbyID(message.getChildID()).getNameChild())
                                .direction(direction)
                                .nameLinea(routeService.getRoutesByID(r.getRouteID()).getNameR())
                                .nameStop(stopService.findStopbyId(r.getStopID()).getNome())
                                .oraFermata(stopService.findStopbyId(r.getStopID()).getTime())
                                .build();
                        messageVMS.add(messageVM);
                    }
                } else if (message.getMessageChildPrenotation() != null) {
                    Reservation reservation = reservationService.findReservationById(message.getReservationID());
                    if (reservation != null) {
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
                                .messageChildPrenotation(true)
                                .directionReservation(reservation.getDirection())
                                .nameLinea(routeService.getRoutesByID(reservation.getRouteID()).getNameR())
                                .nameStop(stopService.findStopbyId(reservation.getStopID()).getNome())
                                .oraFermata(stopService.findStopbyId(reservation.getStopID()).getTime())
                                .build();
                        messageVMS.add(messageVM);
                    }
                }
            } else if (message.getChildID() != null) {       //get messaggio che concerne il bimbo (creazione / cancellazione)
                if (message.getMessageChildCreation() != null) {
                    Child child = childService.findChildbyID(message.getChildID());
                    if (child != null) {
                        MessageVM messageVM = MessageVM.builder()
                                .sender(senderName)
                                .messageID(message.getMessageID().toString())
                                .text(message.getAction())
                                .read(message.getRead())
                                .date(message.getDate())
                                .nameChild(child.getNameChild())
                                .familyName(child.getFamily_name())
                                .messageChildCreation(true)
                                .build();
                        messageVMS.add(messageVM);
                    }
                } else if (message.getMessageChildDelete()) {
                    MessageVM messageVM = MessageVM.builder()
                            .sender(senderName)
                            .messageID(message.getMessageID().toString())
                            .text(message.getAction())
                            .read(message.getRead())
                            .date(message.getDate())
                            .nameChild(message.getNameChild())
                            .familyName(message.getFamilyName())
                            .messageChildDelete(true)
                            .build();
                    messageVMS.add(messageVM);
                }
                // messaggio che concerne il cambio di privilegi visto da un altro user
            } else if (message.getMessageUpdateOtherUser() != null) {
                MessageVM messageVM = MessageVM.builder()
                        .sender(senderName)
                        .messageID(message.getMessageID().toString())
                        .text(message.getAction())
                        .read(message.getRead())
                        .date(message.getDate())
                        .messageUpdateOtherUser(true)
                        .nameLinea(routeService.getRoutesByID(message.getRoute()).getNameR())
                        .build();
                messageVMS.add(messageVM);
                // messaggio che concerne il cambio di privilegi visto dallo user stesso
            } else if (message.getMessageEditAvailability() != null) {
                MessageVM messageVM = MessageVM.builder()
                        .sender(senderName)
                        .messageID(message.getMessageID().toString())
                        .text(message.getAction())
                        .read(message.getRead())
                        .date(message.getDate())
                        .messageEditAvailability(true)
                        .nameLinea(routeService.getRoutesByID(message.getRoute()).getNameR())
                        .build();
                messageVMS.add(messageVM);
                // messaggio che concerne il cambio di privilegi visto dallo user stesso
            } else if (message.getMessageUpdateUser() != null) {
                ArrayList<String> adminRoutesName = new ArrayList<>();
                ArrayList<String> muleRoutesName = new ArrayList<>();
                for (int routeID : message.getAdminRoutes()) {
                    adminRoutesName.add(routeService.getRoutesByID(routeID).getNameR());
                }
                for (int routeID : message.getMuleRoutes()) {
                    muleRoutesName.add(routeService.getRoutesByID(routeID).getNameR());
                }

                MessageVM messageVM = MessageVM.builder()
                        .sender(senderName)
                        .messageID(message.getMessageID().toString())
                        .text(message.getAction())
                        .read(message.getRead())
                        .date(message.getDate())
                        .messageUpdateUser(true)
                        .adminRoutes(adminRoutesName)
                        .muleRoutes(muleRoutesName)
                        .build();
                messageVMS.add(messageVM);

            } else if (message.getMessageChildPrenotation() != null) {
                Reservation r = reservationService.findReservationById(message.getReservationID());
                Boolean direction = ((r.getDirection().equals("andata")) ? true : false);
                System.out.println("stopService.findStopbyId(r.getStopID()).getNome()!!!" + stopService.findStopbyId(r.getStopID()).getNome());
                MessageVM messageVM = MessageVM.builder()
                        .sender(senderName)
                        .messageID(message.getMessageID().toString())
                        .text(message.getAction())
                        .read(message.getRead())
                        .date(message.getDate())
                        .messageChildPrenotation(true)
                        .nameChild(childService.findChildbyID(message.getChildID()).getNameChild())
                        .direction(direction)
                        .nameLinea(routeService.getRoutesByID(r.getRouteID()).getNameR())
                        .nameStop(stopService.findStopbyId(r.getStopID()).getNome())
                        .oraFermata(stopService.findStopbyId(r.getStopID()).getTime())
                        .build();
                messageVMS.add(messageVM);
            }
        }
        return messageVMS;
    }

    @Override
    public void readedUpdated(ObjectId messageID, Boolean read) {
        Message m = findMessageByMessageID(messageID);
        m.setRead(read);
        update(m);
    }

    public Message editStatus(ObjectId messageID, String status) {
        Message m = findMessageByMessageID(messageID);
        if (!m.getStatus().equals("pending"))
            return null;

        if (status.equals("accepted") || status.equals("rejected"))
            m.setStatus(status);

        update(m);
        return m;
    }
}
