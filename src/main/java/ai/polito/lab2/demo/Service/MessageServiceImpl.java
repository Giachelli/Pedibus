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

    /**
     *
     * @param receiverID id del receiver delle mail
     * @return
     */
    @Override
    public ArrayList<Message> findMessagesByReceiverID(ObjectId receiverID) {

        ArrayList<Message> messages = messageRepo.findAllByReceiverID(receiverID);
        return messages;

    }

    /**
     * Metodo per ricercare il messaggio
     * @param messageID id del messaggio da ricercare
     * @return
     */
    @Override
    public Message findMessageByMessageID(ObjectId messageID) {
        return messageRepo.findMessageByMessageID(messageID);
    }

    /**
     * Metodo per aggiornare il messaggio
     * @param message messaggio da salvare
     */
    @Override
    public void update(Message message) {
        messageRepo.save(message);
    }

    /**
     * Metodo per ricercare il messaggio in base all' id del child
     * @param childID id del chilf
     * @return
     */
    @Override
    public Message findMessageByChildID(ObjectId childID) {
        return messageRepo.findMessageByChildID(childID);
    }


    /**
     * Metodo per creare il messaggio relativo al turno
     * @param senderID id del sender
     * @param receiverID id del receiver
     * @param action titolo del messaggio
     * @param time tempo in cui invio il messaggio
     * @param shiftID id del turno
     * @param typeMessage tipo del messaggio
     */
    @Override
    public void createMessageShift(ObjectId senderID, ObjectId receiverID, String action, long time, ObjectId shiftID, String typeMessage) {


        Message message = Message.builder()
                .senderID(senderID)
                .receiverID(receiverID)
                .action(action)
                .read(false)
                .date(time)
                .shiftID(shiftID)
                .messageShiftRequest(true)
                .status("pending")
                .build();

        messageRepo.save(message);

    }

    /**
     * Metodo per creare il messaggio relativo alla risposta (accettazione/rifiuto) turno
     * @param senderID id del sender
     * @param receivers lista di tutti i reciever del messaggio (gli admin di linea)
     * @param action titolo del messaggio
     * @param time tempo in cui il messaggio è stato inviato
     * @param shiftID id del turno
     * @param status stato del turno (accettato/rifiutato)
     * @param typeMessage tipo del messaggio
     */
    @Override
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
                    .messageShiftRequest(true)
                    .status(status)
                    .build();

            messageRepo.save(message);
        }
    }

    /**
     * Metodo per la creazione del messaggio relativo alla creazione/cancellazione del bimbo
     * @param senderID id del sender
     * @param receiverID id del receiver
     * @param childID id del bimbo che è stato creato/cancellato
     * @param action titolo del messaggio
     * @param time tempo in cui viene inviato il messaggio
     * @param typeMessage tipo del messaggio
     */
    @Override
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

    /**
     * Metodo per la creazione del messaggio relativo alla prenotazione
     * @param senderID id del sender
     * @param receivers lista dei receiver del messaggio (tutti gli accompagnatori registrati per quella linea)
     * @param action titolo del messaggio
     * @param time tempo in cui il messaggio viene inviato
     * @param reservationID id della prenotazione
     * @param typeMessage tipo del messaggio
     */
    @Override
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

    /**
     * Metodo per la creazione del messaggio relativo all'aggiornamento dei privilegi di uno user
     * @param sender username di chi manda il messaggio
     * @param receivers lista dei receivers (tutti gli admin di quella linea per cui i privilegi sono stati aggiornati)
     * @param action titolo del messaggio
     * @param time tempo dell'invio del messaggio
     * @param routeID id della linea per cui sto aggiornando i privilegi
     */
    @Override
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

    /**
     * Metodo per la creazione del messaggio relativo alla cancellazione di un turno
     * @param senderID id del sender
     * @param receiversID lista di id dei vari receivers ( tutti gli accompagnatori della linea)
     * @param action titolo del messaggio
     * @param time tempo in cui il messaggio viene inviato
     * @param shiftID id del turno
     */
    @Override
    public void createMessageDeleteTurns(ObjectId senderID, ArrayList<ObjectId> receiversID, String action, long time, ObjectId shiftID) {

        Shift shift = shiftService.getTurnByID(shiftID);

        /*  Per cancellare nella sezione messagi del richiedente, quello con precedente che indicava turno accettato*/
       /* Message m1 = findMessageByShiftIDAndReceiverID(shiftID, senderID);
        if ( m1 != null)
            messageRepo.deleteByMessageID(m1.getMessageID());
        */
        for (ObjectId id : receiversID) {
            Message m = findMessageByShiftIDAndReceiverID(shiftID, id);
            if ( m != null)
                messageRepo.deleteByMessageID(m.getMessageID());

            Message message = Message.builder()
                    .senderID(senderID)
                    .receiverID(id)
                    .action(action)
                    .shiftID(shiftID)
                    .dateTurns(shift.getDate())
                    .route(shift.getLineaID())
                    .startID(shift.getStartID())
                    .stopID(shift.getStopID())
                    .direzione(shift.isDirection())
                    .muleName(userService.getUserBy_id(shift.getMuleID()).getUsername())
                    .read(false)
                    .messageDeleteTurn(true)
                    .date(time)
                    .build();

            messageRepo.save(message);
        }
    }

    /**
     * Metodo per la creazione del messaggio di cambio di disponibilità
     * @param sender username del sender
     * @param receivers lista di recivers ( tutti gli admin della linea)
     * @param action titolo del messaggio
     * @param time tempo del invio del messaggio
     * @param routeID id della linea
     */
    @Override
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

    /**
     * Metodo per la creazione del messaggio relativo all'impostazione di un nuovo ruolo/nuova disponiblità
     * @param sender username del sender
     * @param receiver id del receiver ( utente a cui è stato cambiato il ruolo/ le disponibilità)
     * @param action titolo del messaggio
     * @param time tempo in cui il messaggio viene inviato
     * @param adminRoutes route per cui mi sono stati i cambiati i privilegi come admin
     * @param muleRoutes route per cui mi sono stati cambiati i privilegi come mule
     */
    @Override
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

    /**
     * Metodo per la crezione del messaggio relativo al nuovo user
     * @param senderID
     * @param receiverID
     * @param action
     * @param time
     */
    @Override
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

    /**
     * Metodo per la creazione del messaggio che il bambino che si trovava alla fermata è stato preso in consegna
     * @param sender
     * @param receiver
     * @param action
     * @param time
     * @param childID
     * @param reservationID
     */
    @Override
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

    /**
     * Metodo per trovare il messaggio in base
     * @param shiftID
     * @return
     */
    @Override
    public Message findMessageByShiftID(ObjectId shiftID) {
        return messageRepo.findMessageByShiftID(shiftID);
    }

    /**
     * Metodo per trovare il messaggio dato uno shift e un receiver
     * @param shiftID
     * @param receiverID
     * @return
     */
    @Override
    public Message findMessageByShiftIDAndReceiverID(ObjectId shiftID, ObjectId receiverID){
        return messageRepo.findMessageByShiftIDAndReceiverID(shiftID,receiverID);
    }

    /**
     * Metodo per cancellare un messaggio
     * @param messageID
     * @return
     */
    @Override
    public int deleteByMessageID(ObjectId messageID) {
        Message m = messageRepo.findMessageByMessageID(messageID);

        if (m.getMessageShiftRequest() != null && m.getMessageShiftRequest() == true) {
            if (m.getStatus().equals("pending"))
                return -1;
            else {
                messageRepo.deleteByMessageID(messageID);
                return 1;
            }
        }
        messageRepo.deleteByMessageID(messageID);
        return 1;
    }

    /**
     * Metodo per ricevere un messaggio
     * @param username
     * @return
     */
    public ArrayList<MessageVM> getMessages(String username) {
        ObjectId receiverID = userService.getUserByUsername(username).get_id();
        ArrayList<Message> messages = findMessagesByReceiverID(receiverID);
        ArrayList<MessageVM> messageVMS = new ArrayList<>();

        for (Message message : messages) {

            String senderName = userService.getUserBy_id(message.getSenderID()).getUsername();
            if (senderName != null){

            //Get messaggio turni
            if (message.getShiftID() != null) {
                Shift shift = shiftService.getTurnByID(message.getShiftID());
                if ( shift != null) {
                    if (message.getMessageShiftRequest()) {
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
                }else{if ( message.getMessageDeleteTurn()) {
                        String pattern = "dd/MM";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        String date = simpleDateFormat.format(new Date(message.getDateTurns()));
                        MessageVM messageVM = MessageVM.builder()
                                .sender(senderName)
                                .messageID(message.getMessageID().toString())
                                .text(message.getAction())
                                .read(message.getRead())
                                .date(message.getDate())
                                .nameStop(stopService.findStopbyId(message.getStartID()).getNome())
                                .oraFermata(stopService.findStopbyId(message.getStartID()).getTime())
                                .nameStopDiscesa(stopService.findStopbyId(message.getStopID()).getNome())
                                .oraFermataDiscesa(stopService.findStopbyId(message.getStopID()).getTime())
                                .shiftID(message.getShiftID().toString())
                                .muleName(message.getMuleName())
                                .messageDeleteTurn(true)
                                .dateShift(date)
                                .direction(message.isDirezione())
                                .nameLinea(routeService.getRoutesByID(message.getRoute()).getNameR())
                                .build();
                        messageVMS.add(messageVM);
                    }
                }
            }else if (message.getMessageNewUser() != null) {
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
        }
        return messageVMS;
    }

    /**
     * Metodo per modificare lo stato del messaggio (se letto o meno)
     * @param messageID
     * @param read
     */
    @Override
    public void readedUpdated(ObjectId messageID, Boolean read) {
        Message m = findMessageByMessageID(messageID);
        m.setRead(read);
        update(m);
    }

    /**
     * metodo per modificare lo stato del messaggio (relativo ad un turno)
     * @param messageID
     * @param status
     * @return
     */
    @Override
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
