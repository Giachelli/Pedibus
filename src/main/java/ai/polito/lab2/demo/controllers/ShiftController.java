package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.*;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.viewmodels.ShiftCreateVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.*;

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


    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @RequestMapping(value = "/shift/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShiftCreateVM>> createShift(@RequestBody List<ShiftCreateVM> shiftVMList) {
        List<ShiftCreateVM> returnedList = new ArrayList<>();

        if (controlDoubleShift(shiftVMList)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PRENOTAZIONE MULE GIA' PRESENTE NEL DB");
        }

        /*
        if(shiftVMList.size() > 25)
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

         */

        for (ShiftCreateVM shiftVM : shiftVMList) {
            if (!shiftVM.control())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PRENOTAZIONE MULE GIA' PRESENTE NEL DB");


            Route route = routeService.getRoutesByID(shiftVM.getLineId());
            if (route == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route non presente nel db, controlla l'id");

            User u = userService.getUserByUsername(shiftVM.getUsername());
            if (u == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il mule selezionato non esiste");


            User admin = userService.getUserByUsername(shiftVM.getUsernameAdmin());
            if (admin == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'admin richiedente non esiste");


            if ((!route.getUsernameMule().contains(shiftVM.getUsername())) || (!route.getUsernameAdmin().contains(shiftVM.getUsernameAdmin())))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utente selezionato non è mule per questa linea o l'admin non è admin per questa linea");




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
                    shift.getTurnID()
            );

            shiftVM.setShiftId(shift.getTurnID().toString());
            returnedList.add(shiftVM);
        }


        return new ResponseEntity<List<ShiftCreateVM>>(returnedList, HttpStatus.CREATED);
    }

    private boolean controlDoubleShift(List<ShiftCreateVM> shiftVMList) {

        for(ShiftCreateVM shift : shiftVMList){

            if (shiftService.controlDoubleTurn(shift.getUsername(),shift.getData(),shift.isDirection())){
                return true;
            }
        }

        return false;
    }


    //get dei turni per una linea (linea id) per un mule (mule ID)
    @RequestMapping(value = "/shift/{routeID}/{muleID}/history", method = RequestMethod.GET)
    public ResponseEntity getMuleShifts(@PathVariable final int routeID,@PathVariable final ObjectId muleID){
        Route r = routeService.getRoutesByID(routeID);
        if(r == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User u = userService.getUserBy_id(muleID);
        if(u == null)
        {
            System.out.println("utente non esistente");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<ShiftCreateVM> shifts = shiftService.getTurns(routeID, muleID);


        System.out.println("per la linea "+r.getNameR()+ " e per il mule con username "+ u.getUsername()+ " " +
                "sono state trovate queste prenotazioni (numero):  "+shifts.size() );

        return new ResponseEntity(shifts, HttpStatus.OK);
    }

    //get dei turni per una linea (linea id)
    @RequestMapping(value = "/shift/{routeID}", method = RequestMethod.GET)
    public ResponseEntity getRouteShifts(@PathVariable final int routeID){
        Route r = routeService.getRoutesByID(routeID);
        if(r == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<ShiftCreateVM> shifts = shiftService.getTurnsRoute(routeID);

        System.out.println("per la linea "+r.getNameR()+ " sono state trovate queste prenotazioni (numero):  "+shifts.size() );

        return new ResponseEntity(shifts, HttpStatus.OK);
    }

    //get dei turni per una linea (linea id) per un mule (mule ID)
    @RequestMapping(value = "/shift/{routeID}/{muleID}", method = RequestMethod.GET)
    public ResponseEntity getMuleShiftsAfter(@PathVariable final int routeID,@PathVariable final ObjectId muleID){
        Route r = routeService.getRoutesByID(routeID);
        if(r == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User u = userService.getUserBy_id(muleID);
        if(u == null)
        {
            System.out.println("utente non esistente");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<ShiftCreateVM> shifts = shiftService.getTurnsDate(routeID, muleID);


        System.out.println("per la linea "+r.getNameR()+ " e per il mule con username "+ u.getUsername()+ " " +
                "sono state trovate queste prenotazioni (numero):  "+shifts.size() );

        return new ResponseEntity(shifts, HttpStatus.OK);
    }

    // delete del turno
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @RequestMapping(value = "/shift/{shiftID}/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteShift(@PathVariable final ObjectId shiftID) {

        Shift s = shiftService.getTurnByID(shiftID);
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(s.getStartID());

        if (checkTimestamp(nowTimeStamp,s.getDate(),stop))
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        if (shiftService.getTurnByID(shiftID) != null)
            shiftService.delete(shiftID);
        else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    // modifica di un turno molto simile alla creazione
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @RequestMapping(value = "/shift/{shiftID}/edit", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity editTurn(@PathVariable final ObjectId shiftID, @RequestBody ShiftCreateVM shiftVM) {

        Shift t = shiftService.getTurnByID(shiftID);

        Stop s1 = stopService.findStopbyId(new ObjectId(shiftVM.getStartShiftId()));
        Stop s2 = stopService.findStopbyId(new ObjectId(shiftVM.getStopShiftId()));

        if (t != null) {
            if (!shiftVM.control())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            User u = userService.getUserByUsername(shiftVM.getUsername());
            if (u == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            User admin = userService.getUserByUsername(shiftVM.getUsernameAdmin());
            if (admin == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            Route route = routeService.getRoutesByID(shiftVM.getLineId());
            if (route == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            t.setDate(shiftVM.getData());
            t.setDirection(shiftVM.isDirection());
            t.setLineaID(shiftVM.getLineId());
            t.setMuleID(u.get_id());
            t.setStartID(s1.get_id());
            t.setStopID(s2.get_id());
            t.setAdminID(admin.get_id());
            t.setStatus("pending");

            shiftService.editTurn(t);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //accettazione/rifiuto di un turno
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @RequestMapping(value = "/shift/{shiftID}/{status}", method = RequestMethod.PUT)
    public ResponseEntity editStatus(@PathVariable final ObjectId shiftID, @PathVariable String status) {

        Shift t = shiftService.getTurnByID(shiftID);

        //caso in cui il turno in questione è già stato modificato
        if(!t.getStatus().equals("pending")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (status.equals("accepted")|| status.equals("rejected")) {
            System.out.println("entrato!!!!!!!!!!!!!!!!!!!!!!!!! status è:" + status);
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

            messageService.createMessageResponse(t.getMuleID(),
                    t.getAdminID(),
                    action,
                    day,
                    t.getTurnID(),
                    status);

            // TODO: fare crezione messaggio per un/più admin di linea(se ce ne sono più di uno (come risposta all'accettazione))
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /*
    @RequestMapping(value = "/turn/confirm/{turnID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private boolean confirmTurn(@PathVariable ObjectId turnID, @RequestBody ConfirmAdminVM confirmAdminVM) {
        Turn t = turnService.getTurnByID(turnID);

        if (t.isConfirmed()) {
            return false; // c'è qualcosa di SBGALIATO
        } else {
            t.setConfirmed(true);
            t.setConfirmAdminID(confirmAdminVM.getAdminID());
            System.out.println("Io: " + confirmAdminVM.getAdminID() + "confirmed this turn TRUE: " + t.getTurnID());
            turnService.save(t);
        }
        return true;
    }

    //da parte dell'admin
    @RequestMapping(value = "/turn/modify/{turnID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private void modifyTurn(@PathVariable ObjectId turnID, @RequestBody ModifyTurnVM modifyTurnVM) {
        Turn t = turnService.getTurnByID(turnID);

        if (t.isConfirmed()) {

        } else {
            // dipende da come viene modificato il turno e da che parametri abilitiamo alla modifica
            t.setConfirmAdminID(modifyTurnVM.getAdminID());
            System.out.println("Io: " + modifyTurnVM.getAdminID() + "confirmed this turn TRUE: " + t.getTurnID());
            turnService.save(t);
        }
    }
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
