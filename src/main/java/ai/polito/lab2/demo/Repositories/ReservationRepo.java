package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Reservation;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepo extends MongoRepository<Reservation, ObjectId> {
    Reservation findReservationById(ObjectId id);
}
