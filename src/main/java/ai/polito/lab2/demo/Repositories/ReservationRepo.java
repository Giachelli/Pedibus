package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.viewmodels.ReservationCalendarVM;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepo extends MongoRepository<Reservation, ObjectId> {
    Reservation findReservationById(ObjectId id);
    List<Reservation> findReservationByChildID(ObjectId child_id);

    List<Reservation> findReservationByChildIDAndAndDateAndStopID(ObjectId child_id, long date, ObjectId stop_id);


    @Override
    List<Reservation> findAll();

    List<Reservation> findReservationByFamilyName(String family_name);

    List<Reservation>  findReservationByChildIDAndDateAndDirection(ObjectId childID, long data, String direction);

    List<Reservation> findReservationByDate(long time);

}
