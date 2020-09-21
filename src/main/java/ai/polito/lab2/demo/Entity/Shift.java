package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Document(collection = "shift")
/**
 * Classe che rappresenta uno shift salvato su Mongo
 */
public class Shift {
    @Id
    private ObjectId turnID; //l'id del turno
    private ObjectId muleID; //id dell'accompagnatore
    private ObjectId AdminID; //id dell'admin
    private ObjectId startID; //id della fermata di partenza
    private ObjectId stopID; //id della fermata di arrivo
    private int lineaID; //id della linea
    private long date;//andata o ritorno
    private boolean direction; //se è settato True = andata , false se è ritorno
    private String status; // tre valori: pending, accepted, rejected


}
