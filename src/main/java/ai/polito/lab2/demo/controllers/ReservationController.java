package ai.polito.lab2.demo.controllers;

//import ai.polito.lab2.demo.AppConfig;

import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    Logger logger = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private RouteService routeService;

    @Autowired
    private StopService stopService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;


    /**
     * Prenotazione di un bimbo da parte del genitore
     * @param id_linea id della linea per cui si vuole prenotare
     * @param data data in millesecondi del giorno
     * @param reservationVM richiesta passata dal front end
     * @return ritorna la prenotazione creata
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @Secured("ROLE_USER")
    @RequestMapping(value = "/reservations/{id_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Prenotazione di un bimbo da parte del genitore")
    public ResponseEntity<ReservationCreatedVM> create(@ApiParam("id della linea selezionata") @PathVariable int id_linea,@ApiParam("timestamp della data selezionata") @PathVariable long data,@ApiParam("reservation creata lato fe") @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {


        Reservation r = reservationService.findReservationByChildIDAndDataAndDirection(reservationVM.getChildID(), data, reservationVM.getDirection());
        if(r != null)
        {
            logger.info("Prenotazione child "+ reservationVM.getChildID()+" sovrascritta");
            reservationService.delete(r.getId());
        }

        if(routeService.getRoutesByID(id_linea) == null)
            return new ResponseEntity("Id linea errato",HttpStatus.BAD_REQUEST);

        if (reservationService.controlName_RouteAndStop(id_linea,reservationVM.getStopID()))
            return new ResponseEntity("Errore nel passaggiodi linea e stop",HttpStatus.BAD_REQUEST); //TODO far tornare un errore


        ReservationCreatedVM reservationCreatedVM = reservationService.createReservation(reservationVM, id_linea, data);

        return new ResponseEntity<ReservationCreatedVM>(reservationCreatedVM,HttpStatus.CREATED);

    }

    /**
     * RICHIESTA per aggiungere un bambino non prenotato ma presente alla fermata
     * @param id_linea id della linea
     * @param data data
     * @param reservationVM richiesta di prenotazione passata dal mule per un bimbo non prenotato
     * @return ritorna la prenotazione salvata dopo che è salvata nel db
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @Secured("ROLE_MULE")
    @RequestMapping(value = "/reservations/add/{id_linea}/{data}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("RICHIESTA per aggiungere un bambino non prenotato ma presente alla fermata")
    public ResponseEntity createNotBooked(@PathVariable int id_linea, @PathVariable long data, @RequestBody ReservationVM reservationVM) throws JsonProcessingException, ParseException {
        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(stopID);

        if (checkTimestamp(nowTimeStamp,data,stop))
        {
            return new ResponseEntity("Errore nella data",HttpStatus.BAD_REQUEST);
        }

        if (reservationService.controlName_RouteAndStop(id_linea,reservationVM.getStopID()))
            return new ResponseEntity<>("Errore nel passaggiodi linea e stop",HttpStatus.BAD_REQUEST);

        ChildReservationVM childReservationVM = reservationService.createNotBookedRes(reservationVM,data,id_linea);

        return new ResponseEntity<>(childReservationVM,HttpStatus.CREATED);
    }


    /**
     * RICHIESTA per confermare o meno presenza del bambino
     * @param id_fermata id della fermata in cui si presenta bimbo
     * @param data i millesecondi della data
     * @param childConfirmVM id del bimbo non prenotato che si è comunque presentato + usrname mule
     * @return
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @Secured({"ROLE_MULE"})
    @RequestMapping(value = "/reservations/{id_fermata}/{data}", method = RequestMethod.PUT)
    @ApiOperation("RICHIESTA per confermare o meno presenza del bambino")
    public ResponseEntity confirmPresence(@PathVariable final String id_fermata, @PathVariable long data, @RequestBody childConfirmVM childConfirmVM) throws JsonProcessingException, ParseException {

        Reservation r = null;
        try {
            r = reservationService.findReservationByStopIDAndDataAndChildID(new ObjectId(id_fermata), data, new ObjectId(childConfirmVM.getChildID()));

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(r.getStopID());

        if (checkTimestamp(nowTimeStamp,data,stop))
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ChildReservationVM childReservationVM = reservationService.confirmPresence(r,data,childConfirmVM,id_fermata);
        return new ResponseEntity(childReservationVM, HttpStatus.OK);
    }

    /**
     * per una certa data ritorna tutti i bambini non prenotati più tutti quelli prenotati per quella linea
     * @param id_linea id linea
     * @param data data
     * @return ritorna tutti i bambini non prenotati più tutti quelli prenotati per quella linea (per ogni fermata)
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{id_linea}/{data}", method = RequestMethod.GET)
    @ApiOperation("Per una certa data ritorna tutti i bambini non prenotati più tutti quelli prenotati per quella linea")
    public ResponseEntity<GetChildrenReservationVM> getPeople(@ApiParam("id della linea passata") @PathVariable int id_linea,@ApiParam("timestamp della data") @PathVariable long  data) throws JsonProcessingException, ParseException {
        logger.info("data richiesta "+data);

        if(routeService.getRoutesByID(id_linea)==null){
            return new ResponseEntity("errore nella linea passata", HttpStatus.BAD_REQUEST);
        }

        try {
            GetChildrenReservationVM reservationsVM = reservationService.returnChild(id_linea, data);
            return new ResponseEntity(reservationsVM,HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.BAD_REQUEST);
        }

    }

    /**
     *
     * @param reservationVM nuova reservation da prendere in carico
     * @param nome_linea nome della linea a cui appertiene la reservation
     * @param data data della reservation in millisecondi
     * @param reservation_id id reservation da aggiornare
     * @return
     */
   /* @Secured("ROLE_USER")
    @RequestMapping(value = "/reservations/{nome_linea}/{data}/{reservation_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Per aggiornare una reservation")
    public ResponseEntity update(@RequestBody ReservationVM reservationVM, @PathVariable String nome_linea, @PathVariable long data, @PathVariable final ObjectId reservation_id) {

        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());
        Reservation updatedReservation = reservationService.findReservationById(reservation_id);
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(updatedReservation.getStopID());

        if (checkTimestamp(nowTimeStamp,data,stop))
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (data >= 0) {
            updatedReservation.setDate(data);
        }

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
*/
    /**
     * Va a controllare se è possibile cambiare o eliminare la prenotazione
     * @param nowTimeStamp ora odierna
     * @param data data della prenotazione
     * @param stop fermata a cui appartiene la prenotazione
     * @return
     */
    private boolean checkTimestamp(long nowTimeStamp, long data, Stop stop) {
        if(nowTimeStamp > data){
            return true;
        }
        else {
            if(nowTimeStamp == data)
            {
                nowTimeStamp = updateTimeStamp(data, "now");
                long date = updateTimeStamp(data, stop.getTime());
                if (nowTimeStamp > date){
                   return true;
                }
            }
        }
        return false;
    }

    /**
     * funzione per aaggiornare all'ora corretta il timestamp
     * @param data
     * @param time
     * @return
     */
    private long updateTimeStamp(long data, String time) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        if(time != "now")
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
     * timestamp odierno a mezzanotte
     * @return
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

    /**
     * Delete della reservation
     * @param reservation_id id della reservation da eliminare
     * @return
     */
    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.DELETE)
    @ApiOperation("Eliminazione della reservation")
    public ResponseEntity delete(@ApiParam("Id della prenotazione da eliminare")@PathVariable ObjectId reservation_id) {
        Reservation updatedReservation = reservationService.findReservationById(reservation_id);
        long nowTimeStamp = getCurrentTimeStamp();
        Stop stop = stopService.findStopbyId(updatedReservation.getStopID());

        if (checkTimestamp(nowTimeStamp,updatedReservation.getDate(),stop))
        {
            return new ResponseEntity<>("Errore non è possibile cancellare la prenotazione",HttpStatus.BAD_REQUEST);
        }
        reservationService.delete(reservation_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations/{reservation_id}", method = RequestMethod.GET)
    public ResponseEntity<Reservation> getPeople(@PathVariable ObjectId reservation_id) throws JsonProcessingException {
        Reservation request = reservationService.findReservationById(reservation_id);

        return new ResponseEntity<>(request,HttpStatus.OK);
    }

    /**
     * Funzione per il calendario del genitore
     * @param family_name cognome della famiglia
     * @return tutte le prenotazioni per quella famiglia
     * @throws JsonProcessingException
     */
    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations", method = RequestMethod.GET)
    public ResponseEntity getChildReservation(@RequestParam (required = true) String family_name) throws JsonProcessingException {
        System.out.println("family_name :" + family_name);
        ArrayList<ReservationCalendarVM> reservationCalendarVMS = new ArrayList<>();
        reservationCalendarVMS = reservationService.reservationFamily(family_name);
        return ok().body(reservationCalendarVMS);
    }

    /**
     * Scaricare tutte le prenotazioni per un bimbo per l'admin
     * @param childID id del bambino per cui si vogliono prendere tutte le prenotazioni
     * @return
     * @throws JsonProcessingException
     */
    @Secured({"ROLE_SYSTEM_ADMIN"})
    @RequestMapping(value = "/reservations/child/{childID}", method = RequestMethod.GET)
    public ResponseEntity getChildListReservations(@PathVariable ObjectId childID) throws JsonProcessingException {
        System.out.println("childID :" + childID);
        ArrayList<ReservationCalendarVM> reservationCalendarVMS = new ArrayList<>();
        reservationCalendarVMS = reservationService.reservationsChild(childID);
        return ok().body(reservationCalendarVMS);
    }


    // A livello stilistico era meglio farlo con il PathParam ma funziona anche così

    /**
     *
     * @param id
     * @return
     * @throws JsonProcessingException
     */
    @Secured({"ROLE_USER", "ROLE_MULE"})
    @RequestMapping(value = "/reservations", method = RequestMethod.DELETE)
    public ResponseEntity deleteChildReservation(@RequestParam (required = true) ObjectId id) throws JsonProcessingException {

        reservationService.delete(id);

        return noContent().build();
    }




}
