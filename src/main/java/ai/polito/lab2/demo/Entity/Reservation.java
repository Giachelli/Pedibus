package ai.polito.lab2.demo.Entity;

import ai.polito.lab2.demo.viewmodels.PersonVM;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "reservation")
    @Data
    @Builder
    public class Reservation {
        @Id
        @ToString.Exclude
        private ObjectId id;

         private PersonVM alunno;
         private ObjectId fermata;
         private int linea;
         private String nome_linea;
         private String direzione;
         //private boolean salire;
         private long data;

    /*public Reservation(String nameA, ObjectId fermata, int linea, String direzione, *//*boolean salire,*//* String data) {
        this.alunno = new PersonVM(nameA, false, false);
        this.fermata = fermata;
        this.linea = linea;
        this.direzione = direzione;
        //this.salire=salire;
        this.data = data;
    }*/
}



