package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StopVM {

    private String stopID;
    private String nameStop;
    private int num_s;
    private String time;
}
