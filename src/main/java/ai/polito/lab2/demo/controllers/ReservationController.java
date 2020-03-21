package ai.polito.lab2.demo.controllers;

//import ai.polito.lab2.demo.AppConfig;

import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Service.ReservationService;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.ChildReservationVM;
import ai.polito.lab2.demo.viewmodels.RegisterVM;
import ai.polito.lab2.demo.viewmodels.Stop_RegistrationVM;
import ai.polito.lab2.demo.viewmodels.ReservationVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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

    //@Autowired
    //private EmailSenderService emailSenderService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ChildRepo childRepo;

    @Autowired
    private RouteRepo routeRepo;

    @Autowired
    private ReservationRepo reservationRepo;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;


    @Secured("ROLE_USER")
    @RequestMapping(value = "/reservations/{nome_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reservation> create(@PathVariable String nome_linea, @PathVariable long data, @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {
        System.out.println("entro qui");
        System.out.println(nome_linea);
        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());

        if(routeRepo.findRouteByNameR(nome_linea) == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); //TODO far tornare un errore


        if (this.controlName_RouteAndStop(nome_linea, stopID))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); //TODO far tornare un errore

        Reservation r = Reservation.builder()
                .childID(childID)
                .stopID(stopID)
                .name_route(nome_linea)
                .direction(reservationVM.getDirection())
                .date(data)
                .build();



        r.setRouteID(routeService.getRoutesByName(r.getName_route()).getId());
        //questo childRepo non dovrebbe essere utilizzato
        r.setBooked(true);
        reservationService.save(r);
        System.out.println(r);
        r = reservationService.findReservationByStopIDAndDataAndChildID(stopID,data,childID);
        System.out.println("Nuova Prenotazione");
        System.out.println(r);
        //        Reservation r = reservationService.createReservation(reservationDTO);
        // String idReservation = r.getId().toString();
        return new ResponseEntity<Reservation>(r,HttpStatus.CREATED);
    }

    //RICHIESTA per aggiungere un bambino non prenotato ma presente alla fermata
    @Secured("ROLE_MULE")
    @RequestMapping(value = "/reservations/add/{nome_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reservation> createNotBooked(@PathVariable String nome_linea, @PathVariable long data, @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {
        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());

        if (this.controlName_RouteAndStop(nome_linea,stopID))
            return null; //TODO far tornare un errore
        Reservation r = Reservation.builder()
                .childID(childID)
                .stopID(stopID)
                .name_route(nome_linea)
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
        return new ResponseEntity<>(r,HttpStatus.CREATED);
    }

    //TODO rivevedere implementazione objectID ChildID in base ad angular

    //RICHIESTA per confermare o meno presenza del bambino ( TODO vedere se si può skippare e fare solo lato angular)
    @Secured({"ROLE_MULE"})
    @RequestMapping(value = "/reservations/{id_fermata}/{data}", method = RequestMethod.PUT)
    public ResponseEntity confirmPresence(@PathVariable final String id_fermata, @PathVariable long data, @RequestBody final String childID) throws JsonProcessingException, ParseException {
        Reservation r = reservationService.findReservationByStopIDAndDataAndChildID(new ObjectId(id_fermata), data, new ObjectId(childID));
        System.out.println("Change presence bambino "+childID+" data "+data+ " stopID "+id_fermata+"from "+r.isInPlace()+" to "+!r.isInPlace());
        r.setInPlace(!r.isInPlace());
        reservationService.save(r);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{nome_linea}/{data}", method = RequestMethod.GET)
    public ResponseEntity getPeople(@PathVariable String nome_linea, @PathVariable long data) throws JsonProcessingException, ParseException {

        Route route = routeService.getRoutesByName(nome_linea);
        ArrayList<ChildReservationVM> notBookedA = new ArrayList<>();
        ArrayList<ChildReservationVM> notBookedR = new ArrayList<>();
        ArrayList<Child> allChildren = (ArrayList<Child>) childRepo.findAll();
        ArrayList<Child> children = new ArrayList<>();
        children.addAll(allChildren);

        // nella MAPPA salire ci sono tutti i bimbi prenotati per una certa linea in una certa data
        // la chiave della mappa è il nome della fermata, value è una lista di utenti prenotati per quella fermata.
        Map<String, List<ChildReservationVM>> salire = reservationService.findReservationAndata(route.getId(), data);
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
                        .nameFamily(c.getName_family())
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
                                continue;
                            }
                            i++;
                        }
                    }

                    for (Child c : children)
                        notBookedA.add(ChildReservationVM.builder()
                                .childID(c.getChildID().toString())
                                .nameChild(c.getNameChild())
                                .nameFamily(c.getName_family())
                                .booked(false)
                                .inPlace(false)
                                .build());
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

        Map<String, List<ChildReservationVM>> scendere = reservationService.findReservationRitorno(route.getId(), data);
        ArrayList<Stop_RegistrationVM> ritorno = new ArrayList<>();
        children.clear();
        children.addAll(allChildren);

        if (scendere.size() == 0)
            for (Child c : children)
                notBookedR.add(ChildReservationVM.builder()
                        .childID(c.getChildID().toString())
                        .nameChild(c.getNameChild())
                        .nameFamily(c.getName_family())
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
                            if (c.getChildID().equals(p.getChildID()))
                                children.remove(c);
                        }
                    }

                    for (Child c : children)
                        notBookedR.add(ChildReservationVM.builder()
                                .childID(c.getChildID().toString())
                                .nameChild(c.getNameChild())
                                .nameFamily(c.getName_family())
                                .build());
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

        Map<Object, Object> model = new TreeMap<>();
        model.put("nameRoute", route.getNameR());
        model.put("date", data);
        model.put("pathA", andata);
        model.put("pathR", ritorno);
        model.put("resnotBookedA", notBookedA);
        model.put("resnotBookedR", notBookedR);
        System.out.println(andata);
        System.out.println(notBookedA);
        return ok().body(model);
    }

    //

    @Secured("ROLE_USER")
    @RequestMapping(value = "/reservations/{nome_linea}/{data}/{reservation_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity update(@RequestBody ReservationVM reservationVM, @PathVariable String nome_linea, @PathVariable long data, @PathVariable final ObjectId reservation_id) {

        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());

        Reservation updatedReservation = reservationRepo.findReservationById(reservation_id);

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

    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable ObjectId reservation_id) {

        reservationService.delete(reservation_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.GET)
    public ResponseEntity<Reservation> getPeople(@PathVariable ObjectId reservation_id) throws JsonProcessingException {
        Reservation request = reservationService.findReservationById(reservation_id);

        return new ResponseEntity<>(request,HttpStatus.OK);
    }

    private boolean controlName_RouteAndStop(String name_route, ObjectId stopID) {
        Route route = routeService.getRoutesByName(name_route);
        boolean found = false;
        if(route == null)
            return true;
        else {
            for (Stop s : route.getStopListA())
            {
                if(s.get_id().toString().equals(stopID.toString()))
                    found = true;
            }
            for (Stop s : route.getStopListB())
            {
                if(s.get_id().toString().equals(stopID.toString()))
                    found = true;
            }
            return !found;
        }
    }


}
