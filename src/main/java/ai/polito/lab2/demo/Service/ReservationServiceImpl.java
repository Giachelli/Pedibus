package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.viewmodels.PersonVM;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.StopRepo;
import ai.polito.lab2.demo.Reservation;
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
    private ReservationRepo reservationRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StopRepo stopRepo;

    public Reservation createReservation(Reservation r) throws JsonProcessingException {
      return  reservationRepo.save(r);
/*        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(r);*/
    }

    public Map<String, List<PersonVM>> findReservationAndata (int linea, long data){
        int i =0;
        Query query = new Query();
        query.addCriteria(Criteria.where("linea").is(linea).and("data").is(data).and("direzione").is("andata"));
        query.with(new Sort(Sort.Direction.ASC, "fermata"));
        List<Reservation> res = mongoTemplate.find(query, Reservation.class);
        Map<String, List<PersonVM>> mappa = new HashMap<>();
        String id_prec="";
        List<String> passeggeri= new LinkedList<>();
        for ( Reservation r: res){
            if (i==0) {
                id_prec = "";
            }
            Stop stop = stopRepo.findStopBy_id(r.getFermata());
            String s =stop.getNome();
            String id=r.getFermata().toString();
            if(!mappa.containsKey(s)){
                mappa.put(s, new LinkedList<PersonVM>());
            }
            mappa.get(s).add(r.getAlunno());

        }
        for (String key : mappa.keySet()) {
            System.out.println("key "+key+" values: "+ mappa.get(key));
        }
        return mappa;
    }

    public Map<String, List<PersonVM>> findReservationRitorno (int linea, long data){
        int i =0;
        Query query = new Query();
        query.addCriteria(Criteria.where("linea").is(linea).and("data").is(data).and("direzione").is("ritorno"));
        query.with(new Sort(Sort.Direction.ASC, "fermata"));
        List<Reservation> res = mongoTemplate.find(query, Reservation.class);
        Map<String, List<PersonVM>> mappa = new HashMap<>();
        String id_prec="";
        List<String> passeggeri= new LinkedList<>();
        for ( Reservation r: res){
            if (i==0) {
                id_prec = "";
            }
            Stop stop = stopRepo.findStopBy_id(r.getFermata());
            String s =stop.getNome();
            String id=r.getFermata().toString();
            if(!mappa.containsKey(s)){
                mappa.put(s, new LinkedList<PersonVM>());
            }
            mappa.get(s).add(r.getAlunno());

        }
        for (String key : mappa.keySet()) {
            System.out.println("key "+key+" values: "+ mappa.get(key));
        }
        return mappa;
    }

    public Reservation update(Reservation reservation) {
        return reservationRepo.save(reservation);
        }

        public void delete(ObjectId reservatio_id){
            Query query = new Query();
            query.addCriteria(Criteria.where("id").is(reservatio_id));
            mongoTemplate.remove(query, Reservation.class);
     }

    public Reservation findReservationByNomeLineaAndDataAndIdPerson(ObjectId id_fermata, long data, String idPerson) {
        Query query = new Query();
        query.addCriteria(Criteria.where("fermata").is(id_fermata).and("data").is(data).and("alunno._id").is(idPerson.toString()));
        List<Reservation> r = mongoTemplate.find(query,Reservation.class);
        System.out.println("ciao");
        return r.get(0);
    }


}


