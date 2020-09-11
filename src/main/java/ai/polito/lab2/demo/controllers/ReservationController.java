package ai.polito.lab2.demo.controllers;

//import ai.polito.lab2.demo.AppConfig;

import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private StopService stopService;

    //@Autowired
    //private EmailSenderService emailSenderService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MessageService messageService;

    // TODO: cambiare con service
    @Autowired
    private ChildRepo childRepo;

    @Autowired
    private ChildService childService;

    @Autowired
    private UserService userService;


    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;


    @Secured("ROLE_USER")
    @RequestMapping(value = "/reservations/{id_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reservation> create(@PathVariable int id_linea, @PathVariable long data, @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {
        System.out.println("entro qui");
        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());

        Reservation r = reservationService.findReservationByChildIDAndData(childID, data);
        if (r != null) {
            reservationService.delete(r.getId());
        }

        if (routeService.getRoutesByID(id_linea) == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); //TODO far tornare un errore


        if (this.controlName_RouteAndStop(id_linea, stopID))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); //TODO far tornare un errore

        r = Reservation.builder()
                .childID(childID)
                .stopID(stopID)
                .familyName(reservationVM.getFamily_name())
                .name_route(routeService.getRoutesByID(id_linea).getNameR())
                .direction(reservationVM.getDirection())
                .date(data)
                .build();


        int routeID = routeService.getRoutesByName(r.getName_route()).getId();

        /*  TODO: DA USARE QUANDO IL MESSAGGIO SARà RICEVUTO DA PIù USER

        List<ObjectId> receiverIDs = new ArrayList<>();

        for (String accompagnatore : routeService.getAccompagnaotori(routeID)){
            receiverIDs.add(userService.getUserByUsername(accompagnatore).get_id());
        }
         */


        r.setRouteID(routeID);

        //questo childRepo non dovrebbe essere utilizzato
        r.setBooked(true);
        reservationService.save(r);


        Child child = childRepo.findChildByChildID(childID);

        ObjectId senderID = userService.getUserByUsername(child.getUsername()).get_id();
        ObjectId receiverID = userService.getUserByUsername("admin@info.it").get_id();


        String action = "Prenotazione bimbo";
        long day = new Date().getTime();

        //TODO: far si che i messaggi vengano inviati agli admin di linea (quindi secondo argomento deve essere un array)
        messageService.createMessageReservation(senderID,
                receiverID,
                action,
                day,
                r.getId());

      /*
        System.out.println("Prima di ricercare la prenotazione nel DB::::::::::" + r);
        r = reservationService.findReservationByStopIDAndDataAndChildID(stopID,data,childID);
        System.out.println("Nuova Prenotazione");
        System.out.println("DOPO di ricercare la prenotazione nel DB::::::::::" + r);
        System.out.println(r);
        //        Reservation r = reservationService.createReservation(reservationDTO);
        // String idReservation = r.getId().toString();
      */
        return new ResponseEntity<Reservation>(r, HttpStatus.CREATED);

    }

    //RICHIESTA per aggiungere un bambino non prenotato ma presente alla fermata
    @Secured("ROLE_MULE")
    @RequestMapping(value = "/reservations/add/{id_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createNotBooked(@PathVariable int id_linea, @PathVariable long data, @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {
        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());

        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(stopID);

        if (checkTimestamp(nowTimeStamp, data, stop)) {
            return new ResponseEntity("Errore nella data", HttpStatus.BAD_REQUEST);
        }

        if (this.controlName_RouteAndStop(id_linea, stopID))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        String direction;

        Reservation r = Reservation.builder()
                .childID(childID)
                .stopID(stopID)
                .direction(reservationVM.getDirection())
                .familyName(childService.findChildbyID(childID).getFamily_name())
                .name_route(routeService.getRoutesByID(id_linea).getNameR())
                .direction(reservationVM.getDirection())
                .date(data)
                .build();

        r.setRouteID(routeService.getRoutesByName(r.getName_route()).getId());
        //questo childRepo non dovrebbe essere utilizzato
        r.setBooked(false);

        r.setInPlace(true);


        reservationService.save(r);
        //  Reservation r = reservationService.createReservation(reservationDTO);
        //  String idReservation = r.getId().toString();
        ChildReservationVM childReservationVM =
                ChildReservationVM.builder()
                        .childID(childID.toString())
                        .nameChild(childService.findChildbyID(childID).getNameChild())
                        .nameFamily(reservationVM.getFamily_name())
                        .booked(false)
                        .inPlace(true)
                        .build();

        return new ResponseEntity<>(childReservationVM, HttpStatus.CREATED);
    }

    //TODO rivevedere implementazione objectID ChildID in base ad angular

    //RICHIESTA per confermare o meno presenza del bambino ( TODO vedere se si può skippare e fare solo lato angular)
    @Secured({"ROLE_MULE"})
    @RequestMapping(value = "/reservations/{id_fermata}/{data}", method = RequestMethod.PUT)
    public ResponseEntity confirmPresence(@PathVariable final String id_fermata, @PathVariable long data, @RequestBody final String childID) throws JsonProcessingException, ParseException {
        Reservation r = reservationService.findReservationByStopIDAndDataAndChildID(new ObjectId(id_fermata), data, new ObjectId(childID));
        System.out.println("Change presence bambino " + childID + " data " + data + " stopID " + id_fermata + "from " + r.isInPlace() + " to " + !r.isInPlace());
        r.setInPlace(!r.isInPlace());
        reservationService.save(r);
        ChildReservationVM childReservationVM = ChildReservationVM.builder()
                .childID(r.getChildID().toString())
                .inPlace(r.isInPlace())
                .booked(r.isBooked())
                .nameFamily(r.getFamilyName())
                .nameChild(childService.findChildbyID(r.getChildID()).getNameChild()).build();
        return new ResponseEntity(childReservationVM, HttpStatus.OK);
    }

    /**
    *
    * @param id_linea è l'id che rappresenta la linea richiesta
    * @param data un log che va a rappresentare i millesecondi della data chiesta
    * @return ritorna le prenotazioni per andata e ritorno per la linea e per la data con l'aggiunta dei bambini non prenotati
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @ApiOperation("ritorna tutti i bambini prenotati per la linea per quella data e tutti i bimbi non prenotati")
    @RequestMapping(value = "/reservations/{id_linea}/{data}", method = RequestMethod.GET)
    public ResponseEntity getPeople(@PathVariable int id_linea, @PathVariable long data) throws JsonProcessingException, ParseException {
        System.out.println("data richiesta " + data);
        Route route = routeService.getRoutesByID(id_linea);
        ArrayList<ChildReservationVM> notBookedA = new ArrayList<>();
        ArrayList<ChildReservationVM> notBookedR = new ArrayList<>();
        ArrayList<Child> allChildren = (ArrayList<Child>) childService.findAllChild();
        ArrayList<Child> children = new ArrayList<>();
        children.addAll(allChildren);

        // nella MAPPA salire ci sono tutti i bimbi prenotati per una certa linea in una certa data
        // la chiave della mappa è il nome della fermata, value è una lista di utenti prenotati per quella fermata.
        Map<String, List<ChildReservationVM>> salire = reservationService.findReservationAndata(route.getId(), data);
        //Map<String, List<ChildReservationVM>> presentiNotBookedA = reservationService.findReservationAndataNotBooked(route.getId(), data);
        salire.forEach((key, value) -> {
            System.out.println("KEEEEEEEEY:::::" + key);
            System.out.println("Valueeeeeeee::::" + value);
        });
        // per ogni fermata contiene una serie di info e la lista dei passeggeri che sarebbe l'arrylist passeggeri.
        ArrayList<Stop_RegistrationVM> andata = new ArrayList<>();
        ArrayList<ChildReservationVM> passeggeri = new ArrayList<>();

        //se il size è uguale a 0, per quella linea, in quella data, non ci sono bimbi prenotati e quindi sono tutti
        //non in grassetto nel listone con il totale dei bimbi
        if (salire.size() == 0) {
            for (Child c : children)
                notBookedA.add(ChildReservationVM.builder()
                        .childID(c.getChildID().toString())
                        .nameChild(c.getNameChild())
                        .nameFamily(c.getFamily_name())
                        .inPlace(false)
                        .booked(false)
                        .build());
        }

        //per ogni stop di una linea definita, se la mappa è nulla, passeggeri sarà nullo e aggiungiamo
        // la info al registrationVM da passare

        for (Stop stop : route.getStopListA()) {

            if (salire.size() == 0) {
                andata.add(Stop_RegistrationVM.builder()
                        .stopID(stop.get_id().toString())
                        .name_stop(stop.getNome())
                        .time(stop.getTime())
                        .passengers(passeggeri)
                        .build());
            }
            //se invece non è nulla controlliamo se nella mappa è presente la chiave definita dal nome
            // della fermata
            else {
                if (salire.get(stop.getNome()) != null) {
                    //se presente aggiungiamo tutti i passeggeri alla relativa fermata nella mappa
                    passeggeri.addAll(salire.get(stop.getNome()));
                    for (ChildReservationVM p : passeggeri) {
                        int i = 0;
                        for (Child c : allChildren) {

                            if (c.getChildID().toString().equals(p.getChildID())) {
                                children.remove(c);
                            }
                            i++;
                        }
                    }


                }


                andata.add(Stop_RegistrationVM.builder()
                        .stopID(stop.get_id().toString())
                        .name_stop(stop.getNome())
                        .time(stop.getTime())
                        .passengers(passeggeri)
                        .build());
                passeggeri = new ArrayList<>();

            }
        }
        if (salire.size() != 0)
            for (Child c : children)
                notBookedA.add(ChildReservationVM.builder()
                        .childID(c.getChildID().toString())
                        .nameChild(c.getNameChild())
                        .nameFamily(c.getFamily_name())
                        .booked(false)
                        .inPlace(false)
                        .build());


        Map<String, List<ChildReservationVM>> scendere = reservationService.findReservationRitorno(route.getId(), data);
        //Map<String, List<ChildReservationVM>> scendereNotBooked = reservationService.findReservationRitornoNotBooked(route.getId(), data);
        ArrayList<Stop_RegistrationVM> ritorno = new ArrayList<>();
        children.clear();
        children.addAll(allChildren);

        if (scendere.size() == 0)
            for (Child c : children)
                notBookedR.add(ChildReservationVM.builder()
                        .childID(c.getChildID().toString())
                        .nameChild(c.getNameChild())
                        .nameFamily(c.getFamily_name())
                        .build());

        for (Stop stop : route.getStopListB()) {
            if (scendere.size() == 0) {
                ritorno.add(Stop_RegistrationVM.builder()
                        .stopID(stop.get_id().toString())
                        .name_stop(stop.getNome())
                        .time(stop.getTime())
                        .passengers(passeggeri)
                        .build());

            } else {

                if (scendere.get(stop.getNome()) != null) {
                    passeggeri.addAll(scendere.get(stop.getNome()));
                    for (ChildReservationVM p : passeggeri) {
                        for (Child c : allChildren) {
                            if (c.getChildID().toString().equals(p.getChildID()))
                                children.remove(c);
                        }
                    }

                }
                ritorno.add(Stop_RegistrationVM.builder()
                        .stopID(stop.get_id().toString())
                        .name_stop(stop.getNome())
                        .time(stop.getTime())
                        .passengers(passeggeri)
                        .build());
                passeggeri = new ArrayList<>();


            }
        }

        if (scendere.size() != 0)
            for (Child c : children)
                notBookedR.add(ChildReservationVM.builder()
                        .childID(c.getChildID().toString())
                        .nameChild(c.getNameChild())
                        .nameFamily(c.getFamily_name())
                        .build());

        Map<Object, Object> model = new TreeMap<>();
        model.put("nameRoute", route.getNameR());
        model.put("date", data);
        model.put("pathA", andata);
        model.put("pathR", ritorno);
        model.put("resnotBookedA", notBookedA);
        model.put("resnotBookedR", notBookedR);
        return ok().body(model);
    }

    //

    @Secured("ROLE_USER")
    @RequestMapping(value = "/reservations/{nome_linea}/{data}/{reservation_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity update(@RequestBody ReservationVM reservationVM, @PathVariable String nome_linea, @PathVariable long data, @PathVariable final ObjectId reservation_id) {

        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());
        Reservation updatedReservation = reservationService.findReservationById(reservation_id);
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(updatedReservation.getStopID());

        if (checkTimestamp(nowTimeStamp, data, stop)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (data >= 0) {
            updatedReservation.setDate(data);
        }

        // TODO per il lab 5 non serve ma controllare qui la corrispondenza tra ChildReservationVM e il Child
        if (!reservationVM.getChildID().toString().isEmpty()) {
            updatedReservation.setChildID(childID);
        }

        if (!reservationVM.getDirection().isEmpty()) {
            updatedReservation.setDirection(reservationVM.getDirection());
        }
        if (reservationVM.getStopID() != null) {
            updatedReservation.setStopID(stopID);
        }
        if (nome_linea.isEmpty()) {
            updatedReservation.setName_route(nome_linea);
            updatedReservation.setRouteID(routeService.findIDRouteByNameR(nome_linea));
        }

        reservationService.save(updatedReservation);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean checkTimestamp(long nowTimeStamp, long data, Stop stop) {
        if (nowTimeStamp > data) {
            return true;
        } else {
            if (nowTimeStamp == data) {
                nowTimeStamp = updateTimeStamp(data, "now");
                long date = updateTimeStamp(data, stop.getTime());
                if (nowTimeStamp > date) {
                    return true;
                }
            }
        }
        return false;
    }

    private long updateTimeStamp(long data, String time) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        if (time != "now") {
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

    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable ObjectId reservation_id) {
        Reservation updatedReservation = reservationService.findReservationById(reservation_id);
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(updatedReservation.getStopID());

        if (checkTimestamp(nowTimeStamp, updatedReservation.getDate(), stop)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        reservationService.delete(reservation_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.GET)
    public ResponseEntity<Reservation> getPeople(@PathVariable ObjectId reservation_id) throws JsonProcessingException {
        Reservation request = reservationService.findReservationById(reservation_id);

        return new ResponseEntity<>(request, HttpStatus.OK);
    }


    // TODO: da cancellare probably


   /* @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations", method = RequestMethod.GET)
    public ResponseEntity<List<Reservation>> getChildReservation(@RequestParam (required = true) String family_name) throws JsonProcessingException {
        System.out.println("CHILDID :" + childID);
        List<Reservation> request = reservationService.findReservationByChildID(childID);

        return new ResponseEntity<>(request,HttpStatus.OK);
    }*/

    // TODO: prendersi anche l'ora della fermata
    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations", method = RequestMethod.GET)
    public ResponseEntity getChildReservation(@RequestParam(required = true) String family_name) throws JsonProcessingException {
        System.out.println("family_name :" + family_name);
        ArrayList<ReservationCalendarVM> reservationCalendarVMS = new ArrayList<>();
        reservationCalendarVMS = reservationService.reservationFamily(family_name);
        return ok().body(reservationCalendarVMS);
    }

    @Secured({"ROLE_SYSTEM_ADMIN"})
    @RequestMapping(value = "/reservations/child/{childID}", method = RequestMethod.GET)
    public ResponseEntity getChildListReservations(@PathVariable ObjectId childID) throws JsonProcessingException {
        System.out.println("childID :" + childID);
        ArrayList<ReservationCalendarVM> reservationCalendarVMS = new ArrayList<>();
        reservationCalendarVMS = reservationService.reservationsChild(childID);
        return ok().body(reservationCalendarVMS);
    }


    // A livello stilistico era meglio farlo con il PathParam ma funziona anche così
    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations", method = RequestMethod.DELETE)
    public ResponseEntity deleteChildReservation(@RequestParam(required = true) ObjectId id) throws JsonProcessingException {

        reservationService.delete(id);

        return noContent().build();
    }


    private boolean controlName_RouteAndStop(int id_route, ObjectId stopID) {
        Route route = routeService.getRoutesByID(id_route);
        boolean found = false;
        if (route == null)
            return true;
        else {
            for (Stop s : route.getStopListA()) {
                if (s.get_id().toString().equals(stopID.toString()))
                    found = true;
            }
            for (Stop s : route.getStopListB()) {
                if (s.get_id().toString().equals(stopID.toString()))
                    found = true;
            }
            return !found;
        }
    }


}
