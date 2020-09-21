package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.controllers.ReservationController;
import ai.polito.lab2.demo.viewmodels.*;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.StopRepo;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Stop;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private RouteService routeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ReservationRepo reservationRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StopRepo stopRepo;

    @Autowired
    private ChildService childService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChildRepo childRepo;

    @Autowired
    private ShiftService shiftService;

    String firstDay;

    Logger logger = LoggerFactory.getLogger(ReservationController.class);

    /**
     * Trova tutte le reservation recenti per un bambino
     * @param childID id del bambino
     * @param data timestamp del giorno
     * @return
     */
    public Reservation findRecentReservation(ObjectId childID, long data) {
        Query query = new Query();
        query.addCriteria(Criteria.where("childID").is(childID).and("date").gt(data));
        query.with(new Sort(Sort.Direction.ASC, "date"));
        List<Reservation> res = mongoTemplate.find(query, Reservation.class);
        if (res.size() == 0) {
            logger.info("Nessuna prenotazione attiva");
            return null;
        } else {
            logger.info("Esiste almeno una prenotazione recente");
            return res.get(0);
        }
    }

    /**
     * funzione per calcolare quanti giorni mancano all'inizio della scuola,
     * o da quanti giorni è iniziata
     * @return il numero di giorni a/dall'inizio della scuola
     */
    @Override
    public int calculateFirstDay() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        int day = Integer.parseInt(this.firstDay.split("/")[0]);
        int month = Integer.parseInt(this.firstDay.split("/")[1]) - 1;
        int year = Integer.parseInt(this.firstDay.split("/")[2]);
        Calendar startSchool = new Calendar.Builder().setDate(year, month, day).build();
        startSchool.set(Calendar.MILLISECOND, 0);
        startSchool.set(Calendar.SECOND, 0);
        startSchool.set(Calendar.MINUTE, 0);
        startSchool.set(Calendar.HOUR_OF_DAY, 0);
        int daysBetween = (int) ChronoUnit.DAYS.between(today.toInstant(), startSchool.toInstant());
        return daysBetween;
    }

    /**
     * funzione per settare il primo giorno di scuola
     * @param s stringa che rappresenta il
     */
    @Override
    public void setFirstDay(String s) {
        this.firstDay = s;
    }

    /**
     * Funzione che ritorna il numero di prenotazioni per il giorno odierno
     * @return il numero
     */
    @Override
    public int findNumberReservationToday() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);

        List<Reservation> reservationList = reservationRepo.findReservationByDate(today.getTimeInMillis());

        if (reservationList != null)
            return reservationList.size();
        else
            return 0;
    }

    /**
     * Creare la reservation
     * @param reservationVM campi passati da angular
     * @param id_linea
     * @param data
     * @return
     */
    @Override
    public ReservationCreatedVM createReservation(ReservationVM reservationVM, int id_linea, long data) {

        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());

        Reservation r = Reservation.builder()
                .childID(childID)
                .stopID(stopID)
                .familyName(reservationVM.getFamily_name())
                .name_route(routeService.getRoutesByID(id_linea).getNameR())
                .direction(reservationVM.getDirection())
                .date(data)
                .build();


        int routeID = routeService.getRoutesByName(r.getName_route()).getId();

        r.setRouteID(routeID);
        r.setBooked(true);
        r = this.save(r);


        Child child = childService.findChildbyID(childID);

        ObjectId senderID = userService.getUserByUsername(child.getUsername()).get_id();

        String action = "Prenotazione bimbo";
        long day = new Date().getTime();

        Set<String> accompagnatori = new HashSet<String>();

        for (String s : routeService.getAccompagnaotori(r.getRouteID())){
            accompagnatori.add(s);
        }

        messageService.createMessageReservation(senderID,
                new ArrayList<>(accompagnatori),
                action,
                day,
                r.getId(),
                "messageChildPrenotation"
        );




        return ReservationCreatedVM.builder()
                .id(r.getId().toString())
                .routeID(r.getRouteID())
                .name_route(r.getName_route())
                .stopID(r.getStopID().toString())
                .childID(r.getChildID().toString())
                .booked(true)
                .inPlace(false)
                .date(data)
                .direction(r.getDirection())
                .familyName(r.getFamilyName()).build();
    }

    /**
     * Trova le reservation per l'Andata per un giorno
     * @param linea id della linea
     * @param data timestamp della data richiesta
     * @return
     */
    public Map<String, List<ChildReservationVM>> findReservationAndata(int linea, long data) {
        logger.info("Entro in findReservationAndata con date " + data);
        int i = 0;
        Query query = new Query();
        query.addCriteria(Criteria.where("routeID").is(linea).and("date").is(data).and("direction").is("andata"));
        query.with(new Sort(Sort.Direction.ASC, "stopID"));
        List<Reservation> res = mongoTemplate.find(query, Reservation.class);
        res.forEach(reservation -> {
            logger.info("RESERVATION::::::::" + reservation);
        });
        Map<String, List<ChildReservationVM>> mappa = new HashMap<>();
        String id_prec = "";
        List<String> passeggeri = new LinkedList<>();
        for (Reservation r : res) {
            if (i == 0) {
                id_prec = "";
            }
            Stop stop = stopRepo.findStopBy_id(r.getStopID());
            Child c = childRepo.findChildByChildID(r.getChildID());
            String s = stop.getNome();
            String id = r.getStopID().toString();
            if (!mappa.containsKey(s)) {
                mappa.put(s, new LinkedList<ChildReservationVM>());
            }
            mappa.get(s).add(ChildReservationVM.builder()
                    .childID(c.getChildID().toString())
                    .nameFamily(c.getFamily_name())
                    .nameChild(c.getNameChild())
                    .inPlace(r.isInPlace())
                    .booked(r.isBooked())
                    .build());
        }
        for (String key : mappa.keySet()) {
            logger.info("key " + key + " values: " + mappa.get(key));
        }
        return mappa;
    }

    /**
     * Trova tutti i bambini per una linea non prenotati
     * @param linea id della linea
     * @param data timestamp della linea
     * @return
     */
    @Override
    public Map<String, List<ChildReservationVM>> findReservationAndataNotBooked(int linea, long data) {
        return null;
    }

    /**
     * Trova tutti i bambini per il ritorno per una linea per una data
     * @param linea id della linea
     * @param data timestamp della data
     * @return lista dei bambini prenotati per una linea
     */
    public Map<String, List<ChildReservationVM>> findReservationRitorno(int linea, long data) {
        int i = 0;
        Query query = new Query();
        query.addCriteria(Criteria.where("routeID").is(linea).and("date").is(data).and("direction").is("ritorno"));
        query.with(new Sort(Sort.Direction.ASC, "stopID"));
        List<Reservation> res = mongoTemplate.find(query, Reservation.class);
        Map<String, List<ChildReservationVM>> mappa = new HashMap<>();
        String id_prec = "";
        List<String> passeggeri = new LinkedList<>();
        for (Reservation r : res) {
            if (i == 0) {
                id_prec = "";
            }
            Stop stop = stopRepo.findStopBy_id(r.getStopID());
            Child c = childRepo.findChildByChildID(r.getChildID());

            String s = stop.getNome();
            String id = r.getStopID().toString();
            if (!mappa.containsKey(s)) {
                mappa.put(s, new LinkedList<ChildReservationVM>());
            }
            mappa.get(s).add(ChildReservationVM.builder()
                    .childID(c.getChildID().toString())
                    .nameFamily(c.getFamily_name())
                    .nameChild(c.getNameChild())
                    .inPlace(r.isInPlace())
                    .booked(r.isBooked())
                    .build());

        }
        for (String key : mappa.keySet()) {
            logger.info("key " + key + " values: " + mappa.get(key));
        }
        return mappa;
    }

    /**
     * trova tutti i bambaini non prenotati per quella linea per quella data
     * @param linea id linea
     * @param data timestamp data
     * @return trova tutti i bambaini non prenotati per quella linea per quella data
     */
    @Override
    public Map<String, List<ChildReservationVM>> findReservationRitornoNotBooked(int linea, long data) {
        return null;
    }

    /**
     * aggiorna la reservation
     * @param reservation
     * @return
     */
    public Reservation update(Reservation reservation) {
        return reservationRepo.save(reservation);
    }

    /**
     * elimina una reservation
     * @param reservatio_id
     */
    public void delete(ObjectId reservatio_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(reservatio_id));
        mongoTemplate.remove(query, Reservation.class);
    }

    /**
     * Salva una reservation
     * @param r reservation da salvare
     * @return
     */
    public Reservation save(Reservation r) {
        return reservationRepo.save(r);
    }

    public Reservation saveAndGet(Reservation r) {
        return reservationRepo.save(r);
    }

    public Reservation findReservationById(ObjectId reservation_id) {
        return reservationRepo.findReservationById(reservation_id);
    }


    public Reservation findReservationByStopIDAndDataAndChildID(ObjectId id_fermata, long data, ObjectId childID) throws Exception {
        List<Reservation> r = reservationRepo.findReservationByChildIDAndAndDateAndStopID(childID,data,id_fermata);

        /*Query query = new Query();
        query.addCriteria(Criteria.where("stopID").is(id_fermata).and("date").is(data).and("childID").is(childID));
        List<Reservation> r = mongoTemplate.find(query, Reservation.class);*/
        if(r.size() == 0)
        {
            logger.error("Errore nella richiesta, non esiste un bimbo prenotato per questa data in quella fermata");
            throw new Exception("Errore nella richiesta, non esiste un bimbo prenotato per questa data in quella fermata");
        }

        return r.get(0);
    }

    public List<Reservation> findReservationByChildID(ObjectId child_id) {
        return reservationRepo.findReservationByChildID(child_id);
    }

    public List<Reservation> findAll() {
        return reservationRepo.findAll();
    }

    public ArrayList<ReservationCalendarVM> reservationFamily(String family_name) {
        List<Reservation> res = reservationRepo.findReservationByFamilyName(family_name);
        ArrayList<ReservationCalendarVM> rcvms = new ArrayList<>();

        res.forEach(reservation -> {
            SimpleDateFormat data = null;
            Date d = null;
            Stop s = stopRepo.findStopBy_id(reservation.getStopID());
            try {
                data = new SimpleDateFormat("h:mm");

                d = data.parse(s.getTime());
                data.setTimeZone(TimeZone.getTimeZone("UTC"));
                // d = data.parse(s.getTime());
                //TODO reservationFamily
                System.out.println("DDDD" + d.getTime());

                //System.out.println("IIIIIII" + i);
            } catch (ParseException e) {
                System.out.println("ParseException occured: " + e.getMessage());
            }
            ReservationCalendarVM rcvm = ReservationCalendarVM.builder()
                    .id(reservation.getId().toString())
                    .name_route(reservation.getName_route())
                    .direction(reservation.getDirection())
                    .name_stop(s.getNome())
                    .nameChild(childRepo.findChildByChildID(reservation.getChildID()).getNameChild())
                    .color(childRepo.findChildByChildID(reservation.getChildID()).getColor())
                    .date(reservation.getDate())
                    .hour(d.getTime())
                    .build();

            rcvms.add(rcvm);
        });
        return rcvms;
    }

    public ArrayList<ReservationCalendarVM> reservationsChild(ObjectId childID) {
        List<Reservation> res = reservationRepo.findReservationByChildID(childID);
        ArrayList<ReservationCalendarVM> rcvms = new ArrayList<>();
        res.forEach(bubba -> System.out.println("BUBBAAA:::" + bubba.getName_route()));

        res.forEach(reservation -> {
            SimpleDateFormat data = new SimpleDateFormat("hh:mm");

            Date d = null;
            Stop s = stopRepo.findStopBy_id(reservation.getStopID());
            try {
                data = new SimpleDateFormat("hh:mm");

                data.setTimeZone(TimeZone.getTimeZone("UTC"));
                d = data.parse(s.getTime());
                System.out.println("DDDD" + d);

            } catch (ParseException e) {
                System.out.println("ParseException occured: " + e.getMessage());
            }
            ReservationCalendarVM rcvm = ReservationCalendarVM.builder()
                    .id(reservation.getId().toString())
                    .name_route(reservation.getName_route())
                    .direction(reservation.getDirection())
                    .name_stop(s.getNome())
                    .nameChild(childRepo.findChildByChildID(reservation.getChildID()).getNameChild())
                    .color(childRepo.findChildByChildID(reservation.getChildID()).getColor())
                    .date(reservation.getDate())
                    .hour(d.getTime())
                    .build();

            rcvms.add(rcvm);
        });
        return rcvms;
    }

    /**
     * Trovare la prenotazione per un bambino per una data e direzione
     * @param childIDString id del bambino
     * @param data timestamp del giorno
     * @param direction direzione
     * @return
     */
    @Override
    public Reservation findReservationByChildIDAndDataAndDirection(String childIDString, long data, String direction) {

        ObjectId childID = new ObjectId(childIDString);
        List<Reservation> r = reservationRepo.findReservationByChildIDAndDateAndDirection(childID,data,direction);
        if (r.size() == 0) {
            return null;
        } else
            return r.get(0);
    }

    /**
     * Funzione che controlla se lo stop appartiene alla route
     * @param id_route id della route
     * @param stopID id dello stop
     * @return false se tutto ok
     */
    public boolean controlName_RouteAndStop(int id_route, String stopID) {
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

    /**
     * torna tutti i bambini prenotati e non per una linea
     * @param id_linea id della linea
     * @param data timestamp del giorno
     * @return
     * @throws Exception
     */
    @Override
    public GetChildrenReservationVM returnChild(int id_linea, long data) throws Exception {

        ArrayList<ChildReservationVM> notBookedA = new ArrayList<>();
        ArrayList<ChildReservationVM> notBookedR = new ArrayList<>();

        Route route = routeService.getRoutesByID(id_linea);
        ArrayList<Child> allChildren = (ArrayList<Child>) childService.findAllChild();
        if (allChildren.size() == 0)
            throw new Exception("Nessun bambino presente nel db");

        ArrayList<Child> children = new ArrayList<>();
        children.addAll(allChildren);

        /**
         * nella MAPPA salire ci sono tutti i bimbi prenotati per una certa linea in una certa data
         * la chiave della mappa è il nome della fermata, value è una lista di utenti prenotati per quella fermata.
         *
         */
        Map<String, List<ChildReservationVM>> salire = this.findReservationAndata(route.getId(), data);
        /**
         * per ogni fermata contiene una serie di info e la lista dei passeggeri che sarebbe l'arrylist passeggeri.
         *
         */
        ArrayList<Stop_RegistrationVM> andata = new ArrayList<>();
        ArrayList<ChildReservationVM> passeggeri = new ArrayList<>();

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


        Map<String, List<ChildReservationVM>> scendere = this.findReservationRitorno(route.getId(), data);
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


        return GetChildrenReservationVM.builder().date(data)
                .nameRoute(route.getNameR()).pathA(andata)
                .pathR(ritorno).resnotBookedA(notBookedA)
                .resnotBookedR(notBookedR).build();

    }

    /**
     * Funzione che va a confermare o meno se il bimbo è preso in carico
     * @param r reservation del bambino
     * @param data timestamp della data
     * @param childID id del bambino
     * @param id_fermata id della fermata dove è stato preso in carico il bambino
     * @return
     */
    @Override
    public ChildReservationVM confirmPresence(Reservation r, long data, childConfirmVM childID, String id_fermata) {
        ObjectId muleId = userService.getUserByUsername(childID.getUsernameMule()).get_id();
        System.out.println(shiftService.getTurnsByLineaIDMuleIDDateDirection(r.getRouteID(), muleId, r.getDate(), r.getDirection()));
        if(shiftService.getTurnsByLineaIDMuleIDDateDirection(r.getRouteID(), muleId, r.getDate(), r.getDirection()).size()>0) {
            System.out.println(shiftService.getTurnsByLineaIDMuleIDDateDirection(r.getRouteID(), muleId, r.getDate(), r.getDirection()));
            // if (inplace = true) => fai partire messaggio
            logger.info("Change presence bambino " + childID + " data " + data + " stopID " + id_fermata + "from " + r.isInPlace() + " to " + !r.isInPlace());
            r.setInPlace(!r.isInPlace());
            if (r.isInPlace()) {
                String action = "Bambino preso in carico";
                long day = new Date().getTime();
                if (childID.getUsernameMule() == null || childID.getUsernameMule() == "")
                    childID.setUsernameMule("giacomo.chelli4@gmail.com");
                messageService.createMessageChildinPlace(childID.getUsernameMule(), // deve essere il mule che effettua l'azione
                        childService.findChildbyID(r.getChildID()).getUsername(),
                        action,
                        day,
                        childService.findChildbyID(r.getChildID()).getChildID(),
                        r.getId());
            }

            this.save(r);
            ChildReservationVM childReservationVM = ChildReservationVM.builder()
                    .childID(r.getChildID().toString())
                    .inPlace(r.isInPlace())
                    .booked(r.isBooked())
                    .nameFamily(r.getFamilyName())
                    .nameChild(childService.findChildbyID(r.getChildID()).getNameChild()).build();
            return childReservationVM;
        }else{
            return null;
        }
    }

    /**
     * Creazione della prenotazione per un bimbo non prenotato
     * @param reservationVM prenotazione passata dalla richiesta
     * @param data timestamp della data
     * @param id_linea id della linea
     * @return
     */
    @Override
    public ChildReservationVM createNotBookedRes(ReservationVM reservationVM, long data, int id_linea) {
        ObjectId stopID = new ObjectId(reservationVM.getStopID());
        ObjectId childID = new ObjectId(reservationVM.getChildID());

        ObjectId muleId = userService.getUserByUsername(reservationVM.getMuleUsername()).get_id();
        if(shiftService.getTurnsByLineaIDMuleIDDateDirection(id_linea, muleId, data, reservationVM.getDirection()).size()>0){
            List<Reservation>  deleted = reservationRepo.findReservationByChildIDAndDateAndDirection(childID,data,reservationVM.getDirection());

            //eventualmente si può fare una sorta di update qui

            Reservation r = Reservation.builder()
                    .childID(childID)
                    .stopID(stopID)
                    .direction(reservationVM.getDirection())
                    .familyName(childService.findChildbyID(childID).getFamily_name())
                    .name_route(routeService.getRoutesByID(id_linea).getNameR())
                    .date(data)
                    .build();

            if(deleted.size() >0)
                r.setId(deleted.get(0).getId());


            r.setRouteID(routeService.getRoutesByName(r.getName_route()).getId());
            r.setBooked(false);
            r.setInPlace(true);


            r = this.saveAndGet(r);

            logger.info("RESEERVATIOOON appena SALVATAAAAAA:" + r.getDirection());

            //  Reservation r = reservationService.createReservation(reservationDTO);
            //  String idReservation = r.getId().toString();

            //TODO: messaggio per bimbo preso in carico al genitore
            ChildReservationVM childReservationVM =
                    ChildReservationVM.builder()
                            .childID(childID.toString())
                            .nameChild(childService.findChildbyID(childID).getNameChild())
                            .nameFamily(reservationVM.getFamily_name())
                            .booked(false)
                            .inPlace(true)
                            .build();

            String action= "Bambino non prenotato ma preso in carico";
            long day = new Date().getTime();
            if(reservationVM.getMuleUsername() == null ||reservationVM.getMuleUsername() =="")
                reservationVM.setMuleUsername("giacomo.chelli4@gmail.com");
            messageService.createMessageChildinPlace(reservationVM.getMuleUsername(), // deve essere il mule che effettua l'azione
                    childService.findChildbyID(childID).getUsername(),
                    action,
                    day,
                    childService.findChildbyID(childID).getChildID(),
                    r.getId()
            );
            return childReservationVM;

        } else {
            return null;
        }
    }
}


