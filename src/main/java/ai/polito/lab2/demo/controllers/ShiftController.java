package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.Shift;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Service.IUserService;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.Service.ShiftService;
import ai.polito.lab2.demo.viewmodels.ShiftCreateVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class ShiftController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private RouteService routeService;


    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @RequestMapping(value = "/shift/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShiftCreateVM>> createShift(@RequestBody List<ShiftCreateVM> shiftVMList) {

        List<ShiftCreateVM> returnedList = new ArrayList<>();

        /*
        if(shiftVMList.size() > 25)
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

         */

        for (ShiftCreateVM shiftVM : shiftVMList) {
            if (!shiftVM.control())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            Route route = routeService.getRoutesByID(shiftVM.getLineId());
            if (route == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            if ((!route.getUsernameMule().contains(shiftVM.getUsername())) || (!route.getUsernameAdmin().contains(shiftVM.getUsernameAdmin())))
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            User u = userService.getUserByUsername(shiftVM.getUsername());
            if (u == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            User admin = userService.getUserByUsername(shiftVM.getUsernameAdmin());
            if (admin == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);


            ShiftDTO t;
            t = ShiftDTO.builder()
                    .muleID(u.get_id())
                    .adminID(admin.get_id())
                    .lineId(shiftVM.getLineId())
                    .data(shiftVM.getData())
                    .direction(shiftVM.isDirection())
                    .build();

            // metto il turno sul db
            Shift shift = shiftService.save(t);
            //notificare gli admin di linea

            shiftVM.setShiftId(shift.getTurnID().toString());
            returnedList.add(shiftVM);
        }


        return new ResponseEntity<List<ShiftCreateVM>>(returnedList, HttpStatus.CREATED);
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
            t.setAdminID(admin.get_id());

            shiftService.editTurn(t);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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


}
