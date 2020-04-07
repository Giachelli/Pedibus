package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ReservationCalendarVM {
    private String nameChild;
    private String color;
    private String name_route;
    private String direction;
    private String name_stop;
    private long hour;
    private long date;
}
