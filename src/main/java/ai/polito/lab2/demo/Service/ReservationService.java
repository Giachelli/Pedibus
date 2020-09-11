package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ReservationDTO;
import ai.polito.lab2.demo.viewmodels.ChildReservationVM;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.viewmodels.ReservationCalendarVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public interface ReservationService {
    // Reservation createReservation( ReservationDTO r) throws JsonProcessingException;
    Map<String, List<ChildReservationVM>> findReservationAndata(int linea, long data);

    Map<String, List<ChildReservationVM>> findReservationAndataNotBooked(int linea, long data);

    Map<String, List<ChildReservationVM>> findReservationRitorno(int linea, long data);

    Map<String, List<ChildReservationVM>> findReservationRitornoNotBooked(int linea, long data);

    Reservation update(Reservation r);

    void save(Reservation r);

    void delete(ObjectId id);

    Reservation findReservationById(ObjectId reservation_id);

    Reservation findReservationByStopIDAndDataAndChildID(ObjectId id_fermata, long data, ObjectId childID);

    List<Reservation> findReservationByChildID(ObjectId child_id);

    List<Reservation> findAll();

    ArrayList<ReservationCalendarVM> reservationFamily (String family_name);

    ArrayList<ReservationCalendarVM> reservationsChild (ObjectId childID);

    Reservation findReservationByChildIDAndData(ObjectId childID, long data);

    Reservation findRecentReservation(ObjectId childID, long data);

    int calculateFirstDay();

    void setFirstDay(String s);

    int findNumberReservationToday();

}
