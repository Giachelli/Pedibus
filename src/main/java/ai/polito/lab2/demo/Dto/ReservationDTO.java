package ai.polito.lab2.demo.Dto;

import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Reservation;
import ai.polito.lab2.demo.viewmodels.ChildVM;
import ai.polito.lab2.demo.viewmodels.PersonVM;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ReservationDTO {
    private Child child;
    private Stop nomeFermata;
    private int route;
    private String nome_linea;
    private String direzione;
    private long data;

    public Reservation convert() {
        return Reservation.builder()
                .alunno(this.child)
                .fermata(this.nomeFermata)
                .linea(this.getRoute())
                .nome_linea(this.getNome_linea())
                .direzione(this.getDirezione())
                .data(this.getData())
                .build();
    }
}


