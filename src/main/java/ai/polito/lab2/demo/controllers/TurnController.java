package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.Turn;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Service.IUserService;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.Service.TurnService;
import ai.polito.lab2.demo.viewmodels.ConfirmAdminVM;
import ai.polito.lab2.demo.viewmodels.ModifyTurnVM;
import ai.polito.lab2.demo.viewmodels.TurnCreateVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.bus.EventBus;

@RestController
@CrossOrigin
public class TurnController {

    @Autowired
    private IUserService userService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private RouteService routeService;


    @RequestMapping(value = "/turn/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private Turn createTurn(@RequestBody TurnCreateVM turnVM){
        User u = userService.getUserByUsername(turnVM.getUsername());
        Route route = routeService.getRoutesByID(turnVM.getLineId());
        Turn t;
        if(turnVM.isDirection())
        {
            t = Turn.builder()
                    .muleID(u.get_id())
                    .lineaID(turnVM.getLineId())
                    .date(turnVM.getData())
                    .partenzaStopID(route.getStopListA().get(0).get_id())// il primo stop dell'andata è l'ultimo stop del ritorno
                    .arrivoStopID(route.getStopListB().get(0).get_id())// vieversa
                    .confirmed(false)
                    .direction(turnVM.isDirection())
                    .build();
        }
        else {
            t = Turn.builder()
                    .muleID(u.get_id())
                    .lineaID(turnVM.getLineId())
                    .date(turnVM.getData())
                    .partenzaStopID(route.getStopListB().get(0).get_id())// il primo stop dell'andata è l'ultimo stop del ritorno
                    .arrivoStopID(route.getStopListA().get(0).get_id())
                    .confirmed(false)
                    .direction(turnVM.isDirection())
                    .build();
        }
        Turn data = t;

    // metto il turno sul db
        turnService.save(t);
        //notificare gli admin di linea

        return t;
    }

    @RequestMapping(value = "/turn/confirm/{turnID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private boolean confirmTurn(@PathVariable ObjectId turnID, @RequestBody ConfirmAdminVM confirmAdminVM){
    Turn t = turnService.getTurnByID(turnID);

    if(t.isConfirmed())
    {
        return false; // c'è qualcosa di SBGALIATO
    }
    else {
        t.setConfirmed(true);
        t.setConfirmAdminID(confirmAdminVM.getAdminID());
        System.out.println("Io: "+confirmAdminVM.getAdminID()+"confirmed this turn TRUE: "+ t.getTurnID());
        turnService.save(t);
    }
        return true;
    }

    //da parte dell'admin
    @RequestMapping(value = "/turn/modify/{turnID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private void modifyTurn(@PathVariable ObjectId turnID, @RequestBody ModifyTurnVM modifyTurnVM){
        Turn t = turnService.getTurnByID(turnID);

        if(t.isConfirmed())
        {

        }
        else
        {
            // dipende da come viene modificato il turno e da che parametri abilitiamo alla modifica
            t.setConfirmAdminID(modifyTurnVM.getAdminID());
            System.out.println("Io: "+modifyTurnVM.getAdminID()+"confirmed this turn TRUE: "+ t.getTurnID());
            turnService.save(t);
        }
    }

}
