package ai.polito.lab2.demo;

import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;


@Document(collection = "reservation")
    @Data
    public class Reservation {
        @Id
        @ToString.Exclude
        private ObjectId id;

         private Person alunno;
         private ObjectId fermata;
         private int linea;
         private String direzione;
         //private boolean salire;
         private long data;

    /*public Reservation(String nameA, ObjectId fermata, int linea, String direzione, *//*boolean salire,*//* String data) {
        this.alunno = new Person(nameA, false, false);
        this.fermata = fermata;
        this.linea = linea;
        this.direzione = direzione;
        //this.salire=salire;
        this.data = data;
    }*/
}



