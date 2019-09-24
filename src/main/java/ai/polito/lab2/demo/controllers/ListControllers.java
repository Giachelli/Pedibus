package ai.polito.lab2.demo.controllers;

//import ai.polito.lab2.demo.AppConfig;

import ai.polito.lab2.demo.*;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Service.ReservationService;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.PersonVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class ListControllers {

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


    @RequestMapping(value = "/lines", method = RequestMethod.GET)
    public ResponseEntity getAllRoutesInJson() throws JsonProcessingException {
        List<Route> routes = routeService.getAllRoutes();
        ArrayList<String> routesName = new ArrayList<>();
        for (Route r : routes) {
            routesName.add(r.getNameR());
        }

        Map<Object, Object> model = new HashMap<>();
        model.put("lines", routesName);
        return ok(model);
    }

    @RequestMapping(value = "/lines/{nome_linea}", method = RequestMethod.GET)
    public String getAllStopsForLine(@PathVariable String nome_linea) throws JsonProcessingException {

        String s = new String("[");
        Route route = routeService.getRoutesByName(nome_linea);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(route.getStopListA());
        json = json + ow.writeValueAsString(route.getStopListB());
        return json;
    }


    @RequestMapping(value = "/reservations/{nome_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Reservation create(@PathVariable String nome_linea, @PathVariable long data, @RequestBody Reservation reservation) throws JsonProcessingException, ParseException {
        reservation.setLinea(routeRepo.findRouteByNameR(nome_linea).getId());
        reservation.getAlunno().setId(childRepo.findChildByNameChild(reservation.getAlunno().getNameA()).getIdChild().toString());
        reservation.setData(data);
        reservation.getAlunno().setBooked(true);

        Reservation r = reservationService.createReservation(reservation);
        String idReservation = r.getId().toString();
        return r;
    }


    @RequestMapping(value = "/reservations/add/{nome_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Reservation createNotBooked(@PathVariable String nome_linea, @PathVariable long data, @RequestBody Reservation reservation) throws JsonProcessingException, ParseException {
        reservation.setLinea(routeRepo.findRouteByNameR(nome_linea).getId());
        reservation.getAlunno().setId(childRepo.findChildByNameChild(reservation.getAlunno().getNameA()).getIdChild().toString());
        reservation.setData(data);
        reservation.getAlunno().setBooked(false);
        reservation.getAlunno().setPresent(!reservation.getAlunno().isPresent());

        Reservation r = reservationService.createReservation(reservation);
        String idReservation = r.getId().toString();
        return r;
    }

    @RequestMapping(value = "/reservations/{id_fermata}/{data}", method = RequestMethod.PUT)
    public Reservation confirmPresence(@PathVariable final ObjectId id_fermata, @PathVariable long data, @RequestBody final String idPerson) throws JsonProcessingException, ParseException {
        Reservation r = reservationService.findReservationByNomeLineaAndDataAndIdPerson(id_fermata, data, idPerson);
        r.getAlunno().setPresent(!r.getAlunno().isPresent());
        reservationRepo.save(r);
        return r;
    }

    @RequestMapping(value = "/reservations/{nome_linea}/{data}", method = RequestMethod.GET)
    public ResponseEntity getPeople(@PathVariable String nome_linea, @PathVariable long data) throws JsonProcessingException, ParseException {

        Route route = routeService.getRoutesByName(nome_linea);
        ArrayList<PersonVM> notBookedA = new ArrayList<>();
        ArrayList<PersonVM> notBookedR = new ArrayList<>();
        ArrayList<Child> allChildren = (ArrayList<Child>) childRepo.findAll();
        ArrayList<Child> children = new ArrayList<>();
        children.addAll(allChildren);

        Map<String, List<PersonVM>> salire = reservationService.findReservationAndata(route.getId(), data);
        ArrayList<Line_Registration> andata = new ArrayList<>();
        ArrayList<PersonVM> passeggeri = new ArrayList<>();

        if (salire.size() == 0)
            for (Child c : children)
                notBookedA.add(new PersonVM(c.getIdChild().toString(), c.getNameChild()));

        for (Stop stop : route.getStopListA()) {
            if (salire.size() == 0) {
                andata.add(Line_Registration.builder()
                        .idStop(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .passangers(passeggeri)
                        .build());
            } else {
                if (salire.get(stop.getNome()) != null) {

                    passeggeri.addAll(salire.get(stop.getNome()));
                    for (PersonVM p : passeggeri)
                    {
                        int i = 0;
                        for(Child c : allChildren)
                        {

                            if(c.getIdChild().toString().equals(p.getId()))
                            {
                                children.remove(c);
                                continue;
                            }
                            i++;
                        }
                    }

                    for (Child c : children)
                        notBookedA.add(new PersonVM(c.getIdChild().toString(), c.getNameChild()));
                }

                andata.add(Line_Registration.builder()
                        .idStop(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .passangers(passeggeri)
                        .build());
                passeggeri = new ArrayList<>();

            }
        }

        Map<String, List<PersonVM>> scendere = reservationService.findReservationRitorno(route.getId(), data);
        ArrayList<Line_Registration> ritorno = new ArrayList<>();
        children.clear();
        children.addAll(allChildren);

        if (scendere.size() == 0)
            for (Child c : children)
                notBookedR.add(new PersonVM(c.getIdChild().toString(), c.getNameChild()));

        for (Stop stop : route.getStopListB()) {
            if (scendere.size() == 0) {
                ritorno.add(Line_Registration.builder()
                        .idStop(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .passangers(passeggeri)
                        .build());

            } else {

                if (scendere.get(stop.getNome()) != null) {
                    passeggeri.addAll(scendere.get(stop.getNome()));
                    for (PersonVM p : passeggeri)
                    {
                        for(Child c : allChildren)
                        {
                            if(c.getIdChild().toString().equals(p.getId()))
                                children.remove(c);
                        }
                    }

                    for (Child c : children)
                        notBookedR.add(new PersonVM(c.getIdChild().toString(), c.getNameChild()));
                }
                ritorno.add(Line_Registration.builder()
                        .idStop(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .passangers(passeggeri)
                        .build());
                passeggeri = new ArrayList<>();



            }
        }

        Map<Object, Object> model = new TreeMap<>();
        model.put("nameRoute", route.getNameR());
        model.put("date", data);
        model.put("pathA", andata);
        model.put("pathR", ritorno);
        model.put("resnotBookedA",notBookedA);
        model.put("resnotBookedR",notBookedR);
        return ok(model);
    }


    @RequestMapping(value = "/reservations/{nome_linea}/{data}/{reservation_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Reservation update(@RequestBody Reservation reservation, @PathVariable final ObjectId reservation_id) {
        Reservation updatedReservation = reservationRepo.findReservationById(reservation_id);

        if (reservation.getData() >= 0) {
            updatedReservation.setData(reservation.getData());
        }

        // TODO per il lab 5 non serve ma controllare qui la corrispondenza tra PersonVM e il Child
        if (!reservation.getAlunno().getNameA().isEmpty()) {
            updatedReservation.setAlunno(reservation.getAlunno());
        }

        if (!reservation.getDirezione().isEmpty()) {
            updatedReservation.setDirezione(reservation.getDirezione());
        }
        if (reservation.getFermata() != null) {
            updatedReservation.setFermata(reservation.getFermata());
        }
        if (reservation.getLinea() != 0) {
            updatedReservation.setLinea(reservation.getLinea());
        }

        reservationRepo.save(updatedReservation);

        return updatedReservation;
    }

    @RequestMapping(value = "/reservations/{nome_linea}/{data}/{reservation_id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable ObjectId reservation_id) {
        reservationService.delete(reservation_id);
    }

    @RequestMapping(value = "/reservations/{nome_linea}/{data}/{reservation_id}", method = RequestMethod.GET)
    public String getPeople(@PathVariable ObjectId reservation_id) throws JsonProcessingException {
        Reservation request = reservationRepo.findReservationById(reservation_id);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(request);
        return json;
    }

}