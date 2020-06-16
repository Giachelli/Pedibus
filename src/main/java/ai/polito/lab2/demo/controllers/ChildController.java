package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.viewmodels.ChildVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200")

public class ChildController {

    @Autowired
    private ChildRepo childRepo;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private StopService stopService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/register/child", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChildVM> registerChild(@RequestBody ChildVM data) {


        Child child;

        // non sono required lo stopID, la lineaID, la direction e la data
        if (data.getNameChild()== "" || data.getUsername() == null || data.getFamily_name()==null || data.getColor()==null)
            return badRequest().build();
        //caso in cui il child viene iscritto con una linea e una fermata di default
        //TODO aggiungere nel caso il ritorno solito
        if (data.getNameRoute()!=null && data.getDirection() != null && data.getStopID()!=null) {
            int routeID = routeService.findIDRouteByNameR(data.getNameRoute());
            RouteDTO r = routeService.findRouteByNameR(data.getNameRoute());

             child = Child.builder()
                    .nameChild(data.getNameChild())
                    .username(data.getUsername())
                    .family_name(data.getFamily_name())
                    .isMale(data.isMale())
                    .color(data.getColor())
                    .direction(data.getDirection())
                    .stopID(data.getStopID())
                    .nameRoute(data.getNameRoute())
                    .build();

            System.out.println(data.getUsername());


            // TODO: mettere nel child service, non chiamare la repo direttamente
            childRepo.save(child);
            System.out.println("CHILD PRIMA: " + child);


            child = childRepo.findChildByNameChildAndUsername(data.getNameChild(), data.getUsername());
            System.out.println("CHILD DOPO: " + child);
            //todo controllare che entri qui quando sto a creare un bimbo
            if (data.getDirection() != null && !data.getDirection().equals("")) {
                if (data.getDirection().equals("andata") || data.getDirection().equals("ritorno")) {
                    // TODO per ora lo faccio per due giorni ma è da fare per tutto il periodo scolastico
                    // Usare Calendar che permette tramite get(Day_of_the_week) di prendere la data corretta
                    TimeZone timeZone = TimeZone.getTimeZone("UTC");
                    Calendar today = Calendar.getInstance(timeZone);
                    today.set(Calendar.MILLISECOND, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    int day = 0;
                    long dataTimeStamp;
                    int j = 7;
                    for (int i = 0; i < j; i++) {
                        day = today.get(Calendar.DAY_OF_WEEK);
                        dataTimeStamp = today.getTimeInMillis();
                        if(day == 1 || day == 7)
                        {
                            today.add(Calendar.DATE,1);
                            j++;
                            continue;
                        }
                        Reservation reservation = Reservation.builder()
                                .childID(child.getChildID())
                                .familyName(child.getFamily_name())
                                .date(dataTimeStamp)
                                .direction(data.getDirection())
                                .name_route(data.getNameRoute())
                                .routeID(routeID)
                                .stopID(new ObjectId(data.getStopID()))
                                .booked(true)
                                .inPlace(false)
                                .build();

                        reservationService.save(reservation);
                        // aggiungere tramite calendar con set + 1
                        today.add(Calendar.DATE,1);
                    }
                }
                //TODO: fare caso in cui chi iscrive il bambino vuole iscriverlo sia per l'andata che per il ritorno
                /*else if (data.getDirection().equals("andata|ritorno")){
                    long dataTimeStamp = new Date().getTime();
                    for (int i = 0; i<4; i++){
                        String direzione = "";
                        if ( i<2){
                            direzione = "andata";
                        }
                        Reservation reservation = Reservation.builder()
                                .childID(child.getChildID())
                                .familyName(child.getFamily_name())
                                .date(dataTimeStamp)
                                .direction(data.getDirection())
                                .routeID(routeID)
                                .stopID(new ObjectId(data.getStopID()))
                                .booked(true)
                                .inPlace(false)
                                .build();

                        reservationService.save(reservation);

                        dataTimeStamp= dataTimeStamp+86400000;
                    }
                }*/


            }
        }else{ // caso in cui il bambino è iscritto senza fermata e linea di default
             child = Child.builder()
                    .nameChild(data.getNameChild())
                    .username(data.getUsername())
                    .family_name(data.getFamily_name())
                    .isMale(data.isMale())
                    .color(data.getColor())
                    .build();

            System.out.println(data.getUsername());


            // mettere nel child service, non chiamare la repo direttamente
            childRepo.save(child);
            System.out.println("CHILD PRIMA: " + child);


            child = childRepo.findChildByNameChildAndUsername(data.getNameChild(), data.getUsername());
            System.out.println("CHILD DOPO: " + child);
            String action = "Bambino creato";
            long day = new Date().getTime();
            //TODO: da inviare sia all'admin di linea che all'systemAdmin (quindi da mettere anche nell'if sopra con un array per i più receiver)
            messageService.createMessageResponse(userService.getUserByUsername(child.getUsername()).get_id(),
                    userService.getUserByUsername("admin@info.it").get_id(),
                    action,
                    day
            );
        }


        ChildVM data_return = data;
        data_return.setChildID(child.getChildID().toString());
        System.out.println("Arrivo qui e ed esco");
        return ok().body(data_return);
    }

    @RequestMapping(value = "/user/{userID}/children", method = RequestMethod.GET)
    public ResponseEntity getMyChilds(@PathVariable String userID) {


        ArrayList<Child> children = childRepo.findChildByUsername(userID);

        ArrayList<String> childrenName = new ArrayList<>();
        for (Child r : children) {
            childrenName.add(r.getNameChild());

        }

        Map<Object, Object> model = new HashMap<>();
        model.put("children", childrenName);
        return ok(model);
    }
    // vanno aggiunti più query params
    @RequestMapping(value = "/user/children", method = RequestMethod.GET)
    public ResponseEntity getMyChildren(@RequestParam(required = false) String username) {
        System.out.println("entro qui "+username);
        ArrayList<Child> children ;

        ArrayList<ChildVM> childrenVM = new ArrayList<>();


        if(! username.isEmpty() ){
            System.out.println("SONO QUI ");
            children = childRepo.findChildByUsername(username);
            System.out.println("SONO QUI e "+children.size());
        } else {
            children = (ArrayList) childRepo.findAll();
        }

        System.out.println("arrivo qui "+ children.size());
        for (Child r : children) {
            System.out.println("name: " + r.getNameChild());
            if (r.getStopID()!=null && r.getNameRoute()!=null && r.getDirection()!=null){
                String stopName = stopService.findStopbyId(new ObjectId(r.getStopID())).getNome();
                childrenVM.add(
                        ChildVM.builder()
                                .childID(r.getChildID().toString())
                                .nameChild(r.getNameChild())
                                .family_name(r.getFamily_name())
                                .color(r.getColor())
                                .username(r.getUsername())
                                .isMale(r.isMale())
                                .direction(r.getDirection())
                                .nameRoute(r.getNameRoute())
                                .stopID(r.getStopID())
                                .stopName(stopName)
                                .build()
                );
            }else{
                childrenVM.add(
                        ChildVM.builder()
                                .childID(r.getChildID().toString())
                                .nameChild(r.getNameChild())
                                .family_name(r.getFamily_name())
                                .color(r.getColor())
                                .username(r.getUsername())
                                .isMale(r.isMale())
                        .build());
            }
        }

        Map<Object, Object> model = new HashMap<>();
        model.put("childrenVM", childrenVM);
        return ok().body(childrenVM);
    }

    @RequestMapping(value = "/user/child", method = RequestMethod.DELETE)
    public void deleteChild(@RequestParam(required = true) ObjectId childID ) {

        ArrayList<ObjectId> reservations_id = new ArrayList<>();
        for (Reservation r:reservationService.findReservationByChildID(childID)){

            reservations_id.add(r.getId());
        }

        reservations_id.forEach( (x) -> {
            reservationService.delete(x);
        });
        childRepo.deleteById(childID);
    }
}

