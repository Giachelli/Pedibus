package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.viewmodels.ChildAllVM;
import ai.polito.lab2.demo.viewmodels.ChildVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @Autowired
    private ChildService childService;

    @RequestMapping(value = "/register/child", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChildVM> registerChild(@RequestBody ChildVM data) {

        int routeID = 0;
        Child child;
        RouteDTO r;
        String stopName;

        // non sono required lo stopID, la lineaID, la direction e la data
        if (data.getNameChild() == "" || data.getUsername() == null || data.getFamily_name() == null || data.getColor() == null)
            return badRequest().build();

    /* Caso in cui il child viene iscritto con una linea e una fermata di default */

        if (data.getNameRoute()!= null && !data.getNameRoute().isEmpty() && data.getDirection() != null && data.getStopID() != null) {
            // caso in cui ci sia solo andata o solo ritorno
            if (data.getNameRoute().contains("")) {
                int i = 0;
                for (String s : data.getNameRoute()) {
                    if (!s.equals("")) {
                        routeID = routeService.findIDRouteByNameR(data.getNameRoute().get(i));
                        r = routeService.findRouteByNameR(data.getNameRoute().get(i));
                        break;
                    }
                    i++;
                }
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
                        int j = 10;
                        boolean correct = false;
                        int offsetDayOne = reservationService.calculateFirstDay();
                        for (int k = 0; k < j; k++) {
                            if(!correct){
                                if(offsetDayOne > 0){
                                    today.add(Calendar.DATE, offsetDayOne);
                                    correct = true;
                                } else {
                                    j = j + offsetDayOne;
                                    today.add(Calendar.DATE, 1);
                                    correct = true;
                                }
                            }
                            day = today.get(Calendar.DAY_OF_WEEK);
                            dataTimeStamp = today.getTimeInMillis();
                            if (day == 1 || day == 7) {
                                today.add(Calendar.DATE, 1);
                                j++;
                                continue;
                            }
                            Reservation reservation = Reservation.builder()
                                    .childID(child.getChildID())
                                    .familyName(child.getFamily_name())
                                    .date(dataTimeStamp)
                                    .direction(data.getDirection())
                                    .name_route(data.getNameRoute().get(i))
                                    .routeID(routeID)
                                    .stopID(new ObjectId(data.getStopID().get(i)))
                                    .booked(true)
                                    .inPlace(false)
                                    .build();

                            reservationService.save(reservation);
                            // aggiungere tramite calendar con set + 1
                            today.add(Calendar.DATE, 1);
                        }
                    }else{
                        return badRequest().build();
                    }


                    // TODO: il 27/08 arrivato qui (stavo facendo la possibilità di mettere andata e ritorno)


                    //TODO: fare caso in cui chi iscrive il bambino vuole iscriverlo sia per l'andata che per il ritorno
                /*else if (data.getDirection().equals("entrambi")) {
                    for (int k = 0; k <= 1; k++) {
                        String direzione ="andata";
                        if(k==1)
                            direzione="ritorno";
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
                            if (day == 1 || day == 7) {
                                today.add(Calendar.DATE, 1);
                                j++;
                                continue;
                            }
                            Reservation reservation = Reservation.builder()
                                    .childID(child.getChildID())
                                    .familyName(child.getFamily_name())
                                    .date(dataTimeStamp)
                                    .direction(direzione)
                                    .name_route(data.getNameRoute())
                                    .routeID(routeID)
                                    .stopID(new ObjectId(data.getStopID()))
                                    .booked(true)
                                    .inPlace(false)
                                    .build();

                            reservationService.save(reservation);
                            // aggiungere tramite calendar con set + 1
                            today.add(Calendar.DATE, 1);
                        }
                    }
                }*/


                }else{
                    return badRequest().build();
                }
            }else if(!data.getNameRoute().contains("") && data.getNameRoute().size()==2) {
                if (data.getDirection().equals("entrambi")) {
                    System.out.println("COMUNQUE QUI ENTRO");
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

                    childRepo.save(child);

                    for (int k = 0; k < data.getNameRoute().size(); k++) {
                        routeID = routeService.findIDRouteByNameR(data.getNameRoute().get(k));
                        r = routeService.findRouteByNameR(data.getNameRoute().get(k));
                        TimeZone timeZone = TimeZone.getTimeZone("UTC");
                        Calendar today = Calendar.getInstance(timeZone);
                        today.set(Calendar.MILLISECOND, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        int day = 0;
                        long dataTimeStamp;
                        int j = 10;
                        boolean correct = false;
                        int offsetDayOne = reservationService.calculateFirstDay();
                        for (int h = 0; h < j; h++) {
                            if(!correct) {
                                if (offsetDayOne > 0) {
                                    today.add(Calendar.DATE, offsetDayOne);
                                    correct = true;
                                } else {
                                    j = j + offsetDayOne;
                                    today.add(Calendar.DATE, 1);
                                    correct = true;
                                }
                            }
                            day = today.get(Calendar.DAY_OF_WEEK);
                            dataTimeStamp = today.getTimeInMillis();
                            if (day == 1 || day == 7) {
                                today.add(Calendar.DATE, 1);
                                j++;
                                continue;
                            }if (k==1){
                                Reservation reservation = Reservation.builder()
                                        .childID(child.getChildID())
                                        .familyName(child.getFamily_name())
                                        .date(dataTimeStamp)
                                        .direction("ritorno")
                                        .name_route(data.getNameRoute().get(k))
                                        .routeID(routeID)
                                        .stopID(new ObjectId(data.getStopID().get(k)))
                                        .booked(true)
                                        .inPlace(false)
                                        .build();

                                reservationService.save(reservation);
                                // aggiungere tramite calendar con set + 1
                                today.add(Calendar.DATE, 1);
                            }else{
                                Reservation reservation = Reservation.builder()
                                        .childID(child.getChildID())
                                        .familyName(child.getFamily_name())
                                        .date(dataTimeStamp)
                                        .direction("andata")
                                        .name_route(data.getNameRoute().get(k))
                                        .routeID(routeID)
                                        .stopID(new ObjectId(data.getStopID().get(k)))
                                        .booked(true)
                                        .inPlace(false)
                                        .build();

                                reservationService.save(reservation);
                                // aggiungere tramite calendar con set + 1
                                today.add(Calendar.DATE, 1);
                            }
                        }
                    }
                }else{
                    return badRequest().build();
                }
            }else{
                return badRequest().build();
            }
        }else { // caso in cui il bambino è iscritto senza fermata e linea di default
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
            }
            String action = "Bambino creato";
            long day = new Date().getTime();
            //TODO: da inviare sia all'admin di linea che all'systemAdmin (quindi da mettere anche nell'if sopra con un array per i più receiver)
            messageService.createMessageResponse(userService.getUserByUsername(child.getUsername()).get_id(),
                    userService.getUserByUsername("admin@info.it").get_id(),
                    child.getChildID(),
                    action,
                    day,
                    "messageChildCreation"
            );


            ChildVM data_return = data;
            data_return.setChildID(child.getChildID().toString());
            System.out.println("Arrivo qui e ed esco");
            return ok().body(data_return);
        };



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

        HashMap<Integer,ArrayList<String>> stopName = new HashMap<>();
        ArrayList<String> stopName1 = new ArrayList<String>();
        System.out.println("entro qui "+username);
        ArrayList<Child> children ;
        Integer count_bimbi = 0;
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
            stopName1.clear();
            System.out.println("name: " + r.getNameChild());
            Reservation reservation= reservationService.findRecentReservation(r.getChildID(),new Date().getTime());
            if (r.getStopID()!=null && r.getNameRoute()!=null && r.getDirection()!=null){
                if ( r.getNameRoute().contains("")){
                    int i = 0;
                    for (String s : r.getNameRoute()) {
                        if (!s.equals("")) {
                            stopName1.add(stopService.findStopbyId(new ObjectId(r.getStopID().get(i))).getNome());
                            break;
                        }
                        i++;
                    }
                    System.out.println("stopname1" + stopName1);
                    System.out.println("count_bimbi" + count_bimbi);
                    stopName.put(count_bimbi,new ArrayList<>(stopName1));
                    if (r.getDirection().equals("andata") || r.getDirection().equals("ritorno")){
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
                                        //      .nextCorsa(reservation.getName_route() + reservation.getStopID() + reservation.getDirection())
                                        .stopID(r.getStopID())
                                        .stopName(stopName.get(count_bimbi))
                                        .build()
                        );
                    System.out.println("stop name del child" + childrenVM.get(count_bimbi).getStopName());
                    }

                }else if(r.getDirection().equals("entrambi")){
                    for (int i = 0; i<r.getStopID().size(); i++){
                       stopName1.add(stopService.findStopbyId(new ObjectId(r.getStopID().get(i))).getNome());
                    }
                    stopName.put(count_bimbi,new ArrayList<>(stopName1));
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
                                    //      .nextCorsa(reservation.getName_route() + reservation.getStopID() + reservation.getDirection())
                                    .stopID(r.getStopID())
                                    .stopName(stopName.get(count_bimbi))
                                    .build()
                    );
                }
                System.out.println("stop name del child" + childrenVM.get(count_bimbi).getStopName());

            }else{
                childrenVM.add(
                        ChildVM.builder()
                                .childID(r.getChildID().toString())
                                .nameChild(r.getNameChild())
                                .family_name(r.getFamily_name())
                                .color(r.getColor())
                             //   .nextCorsa(reservation.getName_route() + reservation.getStopID() + reservation.getDirection())
                                .username(r.getUsername())
                                .isMale(r.isMale())
                        .build());
            }
         count_bimbi++;
        }
        System.out.println("stopName" + stopName);

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

        String action = "Bambino precedentemente creato cancellato";
        long day = new Date().getTime();
        //TODO: da inviare sia all'admin di linea che all'systemAdmin (quindi da mettere anche nell'if sopra con un array per i più receiver)
        Child child = childRepo.findChildByChildID(childID);
        messageService.createMessageResponse(userService.getUserByUsername(child.getUsername()).get_id(),
                userService.getUserByUsername("admin@info.it").get_id(),
                child.getChildID(),
                action,
                day,
                "messageChildDelete"
        );
        childRepo.deleteById(childID);

    }

    @RequestMapping(value = "/children/all", method = RequestMethod.GET)
    public ResponseEntity getChildren() {
            ArrayList<Child> children = childService.findAllChild();
            ArrayList<ChildAllVM> childrenVM = new ArrayList<>();

            for (Child c: children){
                childrenVM.add(
                        ChildAllVM.builder()
                                .childID(c.getChildID().toString())
                                .nameChild(c.getNameChild())
                                .family_name(c.getFamily_name())
                                .isMale(c.isMale())
                                .username(c.getUsername())
                                .build()
                );
            }
        return new ResponseEntity<ArrayList<ChildAllVM>>(childrenVM, HttpStatus.OK);

    }
}

