package ai.polito.lab2.demo.viewmodels;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class ReservationCreatedVM {

    private String id;
    private String childID;
    private String stopID;
    private int routeID;
    private String name_route;
    private String direction;
    private boolean booked;
    private String familyName;
    private boolean inPlace;
    private long date;
}
