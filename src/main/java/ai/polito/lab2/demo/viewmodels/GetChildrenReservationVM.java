package ai.polito.lab2.demo.viewmodels;


import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class GetChildrenReservationVM {

    private String nameRoute;
    private long date;
    private ArrayList<Stop_RegistrationVM> pathA;
    private ArrayList<Stop_RegistrationVM> pathR;
    private ArrayList<ChildReservationVM> resnotBookedA;
    private ArrayList<ChildReservationVM> resnotBookedR;
}
