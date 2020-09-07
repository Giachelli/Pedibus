package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ReservationDTO;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.viewmodels.ChildReservationVM;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.StopRepo;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.viewmodels.ReservationCalendarVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;
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
    private RouteRepo routeRepo;

    @Autowired
    private ReservationRepo reservationRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StopRepo stopRepo;

    @Autowired
    private ChildRepo childRepo;

    String firstDay;



    /*
    public Reservation createReservation(ReservationDTO r) throws JsonProcessingException {

        Reservation res= Reservation.builder()
                .alunno(r.getAlunno())
                .data(r.getData())
                .direzione(r.getDirezione())
                .fermata(stopRepo.findStopByNome(r.getNomeFermata()).get_id())
                .nome_linea(r.getNome_linea())
                .linea(r.getRoute())
                .build();
        return  reservationRepo.save(res);
    }*/

    public Reservation findRecentReservation(ObjectId childID, long data){
        Query query = new Query();
        System.out.println("data::::::::::::::::::::::"+ data);
        query.addCriteria(Criteria.where("childID").is(childID).and("date").gt(data));
        query.with(new Sort(Sort.Direction.ASC, "date"));
        List<Reservation> res = mongoTemplate.find(query, Reservation.class);
        if (res.size()==0){
            System.out.println("sono nel size = 0::::::::::::"+ data);

            return null;
        }else{
            System.out.println("sono nel else del size = 0::::::::::::"+ data);
            return res.get(0);
        }
    }
    @Override
    public int calculateFirstDay() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        int day = Integer.parseInt(this.firstDay.split("/")[0]);
        int month = Integer.parseInt(this.firstDay.split("/")[1])-1;
        int year = Integer.parseInt(this.firstDay.split("/")[2]);
        Calendar startSchool = new Calendar.Builder().setDate(year,month,day).build();
        startSchool.set(Calendar.MILLISECOND, 0);
        startSchool.set(Calendar.SECOND, 0);
        startSchool.set(Calendar.MINUTE, 0);
        startSchool.set(Calendar.HOUR_OF_DAY, 0);
        int daysBetween = (int) ChronoUnit.DAYS.between(today.toInstant(),startSchool.toInstant());
        return daysBetween;
    }

    @Override
    public void setFirstDay(String s) {
        this.firstDay=s;
    }

    public Map<String, List<ChildReservationVM>> findReservationAndata(int linea, long data) {
        System.out.println("Entro in findReservationAndata con date "+ data);
        int i = 0;
        Query query = new Query();
        query.addCriteria(Criteria.where("routeID").is(linea).and("date").is(data).and("direction").is("andata"));
        query.with(new Sort(Sort.Direction.ASC, "stopID"));
        List<Reservation> res = mongoTemplate.find(query, Reservation.class);
        res.forEach(reservation -> {
            System.out.println("RESERVATION::::::::"+ reservation);
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
            System.out.println("key " + key + " values: " + mappa.get(key));
        }
        return mappa;
    }

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
            System.out.println("key " + key + " values: " + mappa.get(key));
        }
        return mappa;
    }

    public Reservation update(Reservation reservation) {
        return reservationRepo.save(reservation);
    }

    public void delete(ObjectId reservatio_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(reservatio_id));
        mongoTemplate.remove(query, Reservation.class);
    }

    public void save(Reservation r) {
        reservationRepo.save(r);
    }

    public Reservation findReservationById(ObjectId reservation_id) {
        return reservationRepo.findReservationById(reservation_id);
    }


    public Reservation findReservationByStopIDAndDataAndChildID(ObjectId id_fermata, long data, ObjectId childID) {
        Query query = new Query();
        query.addCriteria(Criteria.where("stopID").is(id_fermata).and("date").is(data).and("childID").is(childID));
        List<Reservation> r = mongoTemplate.find(query, Reservation.class);
        return r.get(0);
    }

    public List<Reservation> findReservationByChildID (ObjectId child_id){
       return reservationRepo.findReservationByChildID(child_id);
    }

    public List<Reservation> findAll() {
        return reservationRepo.findAll();
    }

    public ArrayList<ReservationCalendarVM> reservationFamily(String family_name){
       List<Reservation> res = reservationRepo.findReservationByFamilyName(family_name);
       ArrayList<ReservationCalendarVM> rcvms= new ArrayList<>();
       res.forEach(bubba -> System.out.println("BUBBAAA:::" + bubba.getName_route()));

       res.forEach(reservation -> {
        SimpleDateFormat data= null;
           Date d = null;
           Stop s = stopRepo.findStopBy_id(reservation.getStopID());
        try{
            data = new SimpleDateFormat("h:mm");

            d = data.parse(s.getTime());
            data.setTimeZone(TimeZone.getTimeZone("UTC"));
           // d = data.parse(s.getTime());
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

    public ArrayList<ReservationCalendarVM> reservationsChild (ObjectId childID){
        List<Reservation> res = reservationRepo.findReservationByChildID(childID);
        ArrayList<ReservationCalendarVM> rcvms= new ArrayList<>();
        res.forEach(bubba -> System.out.println("BUBBAAA:::" + bubba.getName_route()));

        res.forEach(reservation -> {
            SimpleDateFormat data= new SimpleDateFormat("hh:mm");

            Date d = null;
            Stop s = stopRepo.findStopBy_id(reservation.getStopID());
            try{
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

    @Override
    public Reservation findReservationByChildIDAndData(ObjectId childID, long data) {
        System.out.println("date: " + data);
        Long temp = data;
        System.out.println("temp: " + temp);
        Query query = new Query();
        query.addCriteria(Criteria.where("date").is(temp).and("childID").is(childID));
        List<Reservation> r = mongoTemplate.find(query, Reservation.class);
        if (r.size()==0){
            return null;
        }
        else
            return r.get(0);
    }
}


