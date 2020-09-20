package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.viewmodels.*;
import ai.polito.lab2.demo.Entity.Reservation;
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

    Reservation save(Reservation r);

    Reservation saveAndGet(Reservation r);

    void delete(ObjectId id);

    Reservation findReservationById(ObjectId reservation_id);

    Reservation findReservationByStopIDAndDataAndChildID(ObjectId id_fermata, long data, ObjectId childID) throws Exception;

    List<Reservation> findReservationByChildID(ObjectId child_id);

    List<Reservation> findAll();

    ArrayList<ReservationCalendarVM> reservationFamily (String family_name);

    ArrayList<ReservationCalendarVM> reservationsChild (ObjectId childID);

    Reservation findReservationByChildIDAndDataAndDirection(String childID, long data, String direction);

    Reservation findRecentReservation(ObjectId childID, long data);

    int calculateFirstDay();

    void setFirstDay(String s);

    int findNumberReservationToday();

    ReservationCreatedVM createReservation(ReservationVM reservationVM, int id_linea, long data);

    boolean controlName_RouteAndStop(int id_linea, String stopID);

    GetChildrenReservationVM returnChild(int id_linea, long data) throws Exception;

    ChildReservationVM confirmPresence(Reservation r, long data, childConfirmVM childID, String id_fermata);

    ChildReservationVM createNotBookedRes(ReservationVM reservationVM, long data, int id_linea);
}
