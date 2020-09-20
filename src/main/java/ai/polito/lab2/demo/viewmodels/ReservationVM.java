package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Child;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@Builder
public class ReservationVM {
    private String childID;
    private String stopID;
    private String direction;
    private String family_name;
    private String muleUsername;
}
