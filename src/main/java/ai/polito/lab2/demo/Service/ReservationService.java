package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ReservationDTO;
import ai.polito.lab2.demo.viewmodels.ChildReservationVM;
import ai.polito.lab2.demo.Entity.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;


public interface ReservationService {
    // Reservation createReservation( ReservationDTO r) throws JsonProcessingException;
    Map<String, List<ChildReservationVM>> findReservationAndata(int linea, long data);

    Map<String, List<ChildReservationVM>> findReservationRitorno(int linea, long data);

    Reservation update(Reservation r);

    void save(Reservation r);

    void delete(ObjectId id);

    Reservation findReservationById(ObjectId reservation_id);

    Reservation findReservationByStopIDAndDataAndChildID(ObjectId id_fermata, long data, ObjectId childID);
}
