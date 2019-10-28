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
import ai.polito.lab2.demo.viewmodels.Stop_RegistrationVM;
import ai.polito.lab2.demo.viewmodels.ReservationVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

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
    public Reservation create(@PathVariable String nome_linea, @PathVariable long data, @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {

        if (this.controlName_Route(nome_linea))
            return null; //TODO far tornare un errore
        Reservation r = Reservation.builder()
                .childID(reservationVM.getChildID())
                .stopID(reservationVM.getStopID())
                .name_route(nome_linea)
                .direction(reservationVM.getDirection())
                .date(data)
                .build();


        r.setRouteID(routeService.getRoutesByName(r.getName_route()).getId());
        //questo childRepo non dovrebbe essere utilizzato
        r.setBooked(true);

        //        Reservation r = reservationService.createReservation(reservationDTO);
        // String idReservation = r.getId().toString();
        return r;
    }

    //RICHIESTA per aggiungere un bambino non prenotato ma presente alla fermata
    @Secured("ROLE_MULE")
    @RequestMapping(value = "/reservations/add/{nome_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Reservation createNotBooked(@PathVariable String nome_linea, @PathVariable long data, @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {
        if (this.controlName_Route(nome_linea))
            return null; //TODO far tornare un errore
        Reservation r = Reservation.builder()
                .childID(reservationVM.getChildID())
                .stopID(reservationVM.getStopID())
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
        return r;
    }

    //TODO rivevedere implementazione objectID ChildID in base ad angular

    //RICHIESTA per confermare o meno presenza del bambino ( TODO vedere se si può skippare e fare solo lato angular)
    @Secured("ROLE_MULE")
    @RequestMapping(value = "/reservations/{id_fermata}/{data}", method = RequestMethod.PUT)
    public Reservation confirmPresence(@PathVariable final ObjectId id_fermata, @PathVariable long data, @RequestBody final ObjectId childID) throws JsonProcessingException, ParseException {
        Reservation r = reservationService.findReservationByNomeLineaAndDataAndIdPerson(id_fermata, data, childID);
        r.setInPlace(!r.isInPlace());
        reservationService.save(r);
        return r;
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
                        .stopID(stop.get_id())
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
                        .stopID(stop.get_id())
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
                        .stopID(stop.get_id())
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
                        .stopID(stop.get_id())
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
        return ok(model);
    }

    //

    @Secured("ROLE_USER")
    @RequestMapping(value = "/reservations/{nome_linea}/{data}/{reservation_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Reservation update(@RequestBody ReservationVM reservationVM, @PathVariable String nome_linea, @PathVariable long data, @PathVariable final ObjectId reservation_id) {

        Reservation updatedReservation = reservationRepo.findReservationById(reservation_id);

        if (data >= 0) {
            updatedReservation.setDate(data);
        }

        // TODO per il lab 5 non serve ma controllare qui la corrispondenza tra ChildReservationVM e il Child
        if (!reservationVM.getChildID().toString().isEmpty()) {
            updatedReservation.setChildID(reservationVM.getChildID());
        }

        if (!reservationVM.getDirection().isEmpty()) {
            updatedReservation.setDirection(reservationVM.getDirection());
        }
        if (reservationVM.getStopID() != null) {
            updatedReservation.setStopID(reservationVM.getStopID());
        }
        if (nome_linea.isEmpty()) {
            updatedReservation.setName_route(nome_linea);
            updatedReservation.setRouteID(routeService.findIDRouteByNameR(nome_linea));
        }

        reservationService.save(updatedReservation);

        return updatedReservation;
    }

    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable ObjectId reservation_id) {
        reservationService.delete(reservation_id);
    }

    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.GET)
    public String getPeople(@PathVariable ObjectId reservation_id) throws JsonProcessingException {
        Reservation request = reservationService.findReservationById(reservation_id);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        // TODO -> NON SERVE USARE JSON
        String json = ow.writeValueAsString(request);
        return json;
    }

    private boolean controlName_Route(String name_route) {
        ArrayList<Route> routes = (ArrayList<Route>) routeService.getAllRoutes();
        ArrayList<String> routes_names = new ArrayList<>();
        for (Route route : routes) {

            routes_names.add(route.getNameR());
        }
        ;
        if (!routes_names.contains(name_route))
            return true;
        else {
            return false;
        }
    }


}
