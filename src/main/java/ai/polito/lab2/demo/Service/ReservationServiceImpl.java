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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

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

    public Map<String, List<ChildReservationVM>> findReservationAndata(int linea, long data) {
        System.out.println("Entro in findReservationAndata con date "+ data);
        int i = 0;
        Query query = new Query();
        query.addCriteria(Criteria.where("routeID").is(linea).and("date").is(data).and("direction").is("andata"));
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


}


