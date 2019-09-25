package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationVM {
    private PersonVM alunno;
    private String nomeFermata;
    private String direzione;
}
