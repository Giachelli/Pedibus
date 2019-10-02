package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.ArrayList;

@Data
@Builder
public class Stop_RegistrationVM {
    private ObjectId stopID;
    private String name_stop;
    private String time;
    private ArrayList<ChildReservationVM> passengers;

}
