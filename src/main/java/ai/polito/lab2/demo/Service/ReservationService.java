package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.viewmodels.PersonVM;
import ai.polito.lab2.demo.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;


public interface ReservationService {
     Reservation createReservation( Reservation r) throws JsonProcessingException;
     //Map<String,List<String>> findReservationTrue (int linea, String data);
     //Map<String,List<String>> findReservationFalse (int linea, String data);
     Map<String, List<PersonVM>> findReservationAndata (int linea, long data);
     Map<String, List<PersonVM>> findReservationRitorno (int linea, long data);

     Reservation update (Reservation r);
     void delete (ObjectId id);

     Reservation findReservationByNomeLineaAndDataAndIdPerson(ObjectId id_fermata, long data, String idPerson);
}
