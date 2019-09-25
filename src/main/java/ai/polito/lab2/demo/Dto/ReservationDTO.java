package ai.polito.lab2.demo.Dto;

import ai.polito.lab2.demo.viewmodels.PersonVM;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationDTO {
    private PersonVM alunno;
    private String nomeFermata;
    private int route;
    private String nome_linea;
    private String direzione;
    private long data;

}
