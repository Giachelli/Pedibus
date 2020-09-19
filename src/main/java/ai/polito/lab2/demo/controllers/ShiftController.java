package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.*;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.viewmodels.ShiftCreateVM;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin
public class ShiftController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private StopService stopService;

    @Autowired
    private MessageService messageService;

    private final static Logger logger = LoggerFactory.getLogger(ShiftController.class);

    /**
     * Creazione dei turni da parte dell'admin o system admin
     * @param shiftVMList lista di shift creati per un utente
     * @return ritorna la lista degli shift creati lato db al fe
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @ApiOperation("Creazione di vari shift")
    @RequestMapping(value = "/shift/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShiftCreateVM>> createShift( @RequestBody List<ShiftCreateVM> shiftVMList) {
        List<ShiftCreateVM> returnedList = new ArrayList<>();

        if (controlDoubleShift(shiftVMList)){
           return new ResponseEntity("PRENOTAZIONE MULE GIA' PRESENTE NEL DB",HttpStatus.BAD_REQUEST);
        }


        for (ShiftCreateVM shiftVM : shiftVMList) {
            if (!shiftVM.control())
                return new ResponseEntity("PRENOTAZIONE Errata mancano dei dati",HttpStatus.BAD_REQUEST);


            Route route = routeService.getRoutesByID(shiftVM.getLineId());
            if (route == null)
                return new ResponseEntity("Route non presente nel db, controlla l'id",HttpStatus.BAD_REQUEST );

            User u = userService.getUserByUsername(shiftVM.getUsername());
            if (u == null)
                return new ResponseEntity("Il mule selezionato non esiste",HttpStatus.BAD_REQUEST);


            User admin = userService.getUserByUsername(shiftVM.getUsernameAdmin());
            if (admin == null)
                return new ResponseEntity("L'admin richiedente non esiste",HttpStatus.BAD_REQUEST);


            if ((!route.getUsernameMule().contains(shiftVM.getUsername())) || (!route.getUsernameAdmin().contains(shiftVM.getUsernameAdmin())))
                return new ResponseEntity("L'utente selezionato non è mule per questa linea o l'utente dichiarante non è admin per questa linea",HttpStatus.BAD_REQUEST);


            Stop s1 = stopService.findStopbyId(new ObjectId(shiftVM.getStartShiftId()));
            Stop s2 = stopService.findStopbyId(new ObjectId(shiftVM.getStopShiftId()));

            ShiftDTO t;
            t = ShiftDTO.builder()
                    .muleID(u.get_id())
                    .adminID(admin.get_id())
                    .lineId(shiftVM.getLineId())
                    .data(shiftVM.getData())
                    .direction(shiftVM.isDirection())
                    .startShiftID(s1.get_id())
                    .stopShiftID(s2.get_id())
                    .status("pending")
                    .build();


            // metto il turno sul db
            Shift shift = shiftService.save(t);
            //notificare gli admin di linea

            String action = "Richiesta turno";

            long day = new Date().getTime();

            messageService.createMessageShift(admin.get_id(), u.get_id(),
                    action,
                    day,
                    shift.getTurnID(),
                    "messageShiftRequest"
            );

            shiftVM.setShiftId(shift.getTurnID().toString());
            returnedList.add(shiftVM);
        }

        logger.info("Creazione shifts effettuata con successo per il mule ");
        return new ResponseEntity<List<ShiftCreateVM>>(returnedList, HttpStatus.CREATED);
    }

    /**
     * Funzione che va a controllare che per il mule selezionato non ci sia già una prenotazione di accompagnamento per quella data
     * @param shiftVMList lista deli turni da controllare
     * @return ritorna false se è tutto ok, true se c'è già una prenotazione per quel giorno per quel mule
     */
    private boolean controlDoubleShift(List<ShiftCreateVM> shiftVMList) {

        for(ShiftCreateVM shift : shiftVMList){

            if (shiftService.controlDoubleTurn(shift.getUsername(),shift.getData(),shift.isDirection())){
                return true;
            }
        }

        return false;
    }

    /**
     * get dei turni per una linea per un mule
     *
     * @param routeID id della linea
     * @param muleID
     * @return ritorna i turni prenotati per quel mule su quella linea
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN","ROLE_MULE"})
    @RequestMapping(value = "/shift/{routeID}/{muleID}/history", method = RequestMethod.GET)
    @ApiOperation("get dei turni per una linea per un mule")
    public ResponseEntity getMuleShifts(@ApiParam("id della linea") @PathVariable final int routeID,@ApiParam("id del mule")@PathVariable final ObjectId muleID){
        Route r = routeService.getRoutesByID(routeID);
        if(r == null)
        {
            return new ResponseEntity<>("id della route errato",HttpStatus.BAD_REQUEST);
        }

        User u = userService.getUserBy_id(muleID);
        if(u == null)
        {
            logger.error("utente non esistente");
            return new ResponseEntity<>("mule selezionato non esistente",HttpStatus.BAD_REQUEST);
        }

        List<ShiftCreateVM> shifts = shiftService.getTurns(routeID, muleID);


        logger.info("per la linea "+r.getNameR()+ " e per il mule con username "+ u.getUsername()+ " " +
                "sono state trovate queste prenotazioni (numero):  "+shifts.size() );

        return new ResponseEntity(shifts, HttpStatus.OK);
    }



    /**
     * get dei turni per una linea
     * @param routeID id della linea
     * @return ritorna tutti gli shift per quella linea
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @RequestMapping(value = "/shift/{routeID}", method = RequestMethod.GET)
    @ApiOperation("get dei turni per una linea")
    public ResponseEntity getRouteShifts(@ApiParam("id della linea")@PathVariable final int routeID){
        Route r = routeService.getRoutesByID(routeID);
        if(r == null)
        {
            logger.error("route con id "+routeID+" non esistente");
            return new ResponseEntity<>("route non esistente",HttpStatus.BAD_REQUEST);
        }

        List<ShiftCreateVM> shifts = shiftService.getTurnsRoute(routeID);

        logger.info("per la linea "+r.getNameR()+ " sono state trovate queste prenotazioni (numero):  "+shifts.size() );

        return new ResponseEntity(shifts, HttpStatus.OK);
    }

    /**
     * get dei turni recenti e futuri per una linea per un mule
     * @param routeID id linea
     * @param muleID id mule
     * @return i turni da 7 giorni fa per tutto il futuro per un mule
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN","ROLE_MULE"})
    @RequestMapping(value = "/shift/{routeID}/{muleID}", method = RequestMethod.GET)
    @ApiOperation("get dei turni recenti e futuri per una linea per un mule")
    public ResponseEntity getMuleShiftsAfter(@ApiParam("id della linea")@PathVariable final int routeID,@ApiParam("id del mule")@PathVariable final ObjectId muleID){
        Route r = routeService.getRoutesByID(routeID);
        if(r == null)
        {
            logger.error("route non esistente con id "+routeID);
            return new ResponseEntity<>("route non esistente",HttpStatus.BAD_REQUEST);
        }

        User u = userService.getUserBy_id(muleID);
        if(u == null)
        {
            logger.error("utente non esistente con id "+muleID);
            return new ResponseEntity<>("utente non esistente",HttpStatus.BAD_REQUEST);
        }

        List<ShiftCreateVM> shifts = shiftService.getTurnsDate(routeID, muleID);


        logger.info("per la linea "+r.getNameR()+ " e per il mule con username "+ u.getUsername()+ " " +
                "sono state trovate queste prenotazioni (numero):  "+shifts.size() );

        return new ResponseEntity(shifts, HttpStatus.OK);
    }

    /**
     * prende tutte i turni recenti e future per una linea
     * @param routeID id della linea
     * @return ritorna tutti i turni recenti e futuri per una linea
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @RequestMapping(value = "/shift/present/{routeID}", method = RequestMethod.GET)
    @ApiOperation("prende tutte i turni recenti e future per una linea")
    public ResponseEntity getShiftsAfter(@ApiParam("id della linea")@PathVariable final int routeID){
        Route r = routeService.getRoutesByID(routeID);
        if(r == null)
        {
            logger.error("route non esistente con id "+routeID);
            return new ResponseEntity<>("route non esistente",HttpStatus.BAD_REQUEST);
        }

        List<ShiftCreateVM> shifts = shiftService.getAllTurnsDate(routeID);

        return new ResponseEntity(shifts, HttpStatus.OK);
    }


    /**
     * Eliminazione di un turno
     * @param shiftID id del turno da eliminare
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @ApiOperation("Eliminazione di un turno")
    @RequestMapping(value = "/shift/{shiftID}/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteShift(@ApiParam("id dello shift")@PathVariable final ObjectId shiftID) {

        Shift s = shiftService.getTurnByID(shiftID);
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(s.getStartID());

        if (checkTimestamp(nowTimeStamp,s.getDate(),stop))
        {
            return new ResponseEntity<>("turno non più eliminabile",HttpStatus.BAD_REQUEST);
        }


        if (shiftService.getTurnByID(shiftID) != null)
            shiftService.delete(shiftID);
        else
            return new ResponseEntity<>("Errore nella eliminazione del turno",HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    /**
     * Accettazione o rifiuto di un turno
     * @param shiftID id del turno
     * @param status stato del turno
     * @return
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @RequestMapping(value = "/shift/{shiftID}/{status}", method = RequestMethod.PUT)
    public ResponseEntity editStatus(@ApiParam("id dello shift")@PathVariable final ObjectId shiftID, @ApiParam("status")@PathVariable String status) {

        Shift t = shiftService.getTurnByID(shiftID);

        Route route = routeService.getRoutesByID(t.getLineaID());


        /**
         * caso in cui il turno in questione è già stato modificato
         */
        if(!t.getStatus().equals("pending")) {
            return new ResponseEntity<>("Turno non più pending",HttpStatus.BAD_REQUEST);
        }

        if (status.equals("accepted")|| status.equals("rejected")) {
            //System.out.println("entrato!!!!!!!!!!!!!!!!!!!!!!!!! status è:" + status);
            t.setStatus(status);

            shiftService.editTurn(t);

            Message message = messageService.findMessageByShiftID(shiftID);

            message.setStatus(status);
            message.setRead(true);
            messageService.update(message);

            String stato = "";
            if(status.equals("accepted"))
                stato="Accettato";
            else if(status.equals("rejected"))
                stato="Rifiutato";

            String action = "Turno " + stato;

            long day = new Date().getTime();
            ArrayList<String> accompagnatori = new ArrayList<>(route.getUsernameAdmin());
            /**
             * messaggio che arriva a tutti gli admin di linea (non lo faccio arrivare ai muli, che sarebbe un bordello andare a vedere i doppioni (mi creo altirmenti un altra struttura) tranne a quello che ha appena accettato
             */
            if (accompagnatori.contains(userService.getUserBy_id(t.getMuleID()).getUsername())){
                accompagnatori.remove(userService.getUserBy_id(t.getMuleID()).getUsername());  // in questo modo il messaggio non dovrebbe arrivare a chi ha fatto l'operazione anche se admin di un altra linea per cui lo user ha subito delle variazioni
            }
            messageService.createMessageResponse(t.getMuleID(),
                    accompagnatori,
                    action,
                    day,
                    t.getTurnID(),
                    status,
                    "messageShiftResponse");

            // TODO: fare crezione messaggio per un/più admin di linea(se ce ne sono più di uno (come risposta all'accettazione))
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            return new ResponseEntity<>("Errore nell'accetazione del turno",HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Funzione che controlla se un turno è ancora modificabile o meno
     * @param nowTimeStamp ora odierna in millisecondi
     * @param data data del turno in millesecondi
     * @param stop stop a cui il turno è assegnato
     * @return
     */
    private boolean checkTimestamp(long nowTimeStamp, long data, Stop stop) {
        if(nowTimeStamp > data){
            return true;
        }
        else {
            if(nowTimeStamp == data)
            {
                nowTimeStamp = updateTimeStamp(data, "Now");
                long date = updateTimeStamp(data, stop.getTime());
                if (nowTimeStamp > date){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * funzione per aggiornare i millesecondi
     * @param data data di partenza in millesecondi
     * @param time ora da aggiungere alla data
     * @return ritorna i millesecondi aggiornati
     */
    private long updateTimeStamp(long data, String time) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        if(time != "Now")
        {
            String hour = time.split(":")[0];
            String minutes = time.split(":")[1];
            today.set(Calendar.MINUTE, Integer.valueOf(minutes));
            today.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
        }
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);

        return today.getTimeInMillis();
    }

    /**
     *
     * @return ritorna il timestamp corrente (oggi a mezzanotte)
     */
    private long getCurrentTimeStamp() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        return today.getTimeInMillis();
    }


}
