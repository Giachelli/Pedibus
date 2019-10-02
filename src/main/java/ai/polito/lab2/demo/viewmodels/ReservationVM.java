package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Child;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@Builder
public class ReservationVM {
    private ObjectId childID;
    private ObjectId stopID;
    private String direction;
}
