package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Family;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationVM {
    private Family family;
    private String name_alunno;
    private String nomeFermata;
    private String direzione;
}
