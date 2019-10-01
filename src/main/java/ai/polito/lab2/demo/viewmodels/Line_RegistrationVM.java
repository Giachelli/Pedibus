package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.viewmodels.PersonVM;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class Line_RegistrationVM {
    private String idStop;
    private String nameStop;
    private String time;
    private ArrayList<PersonVM> passangers;

}
