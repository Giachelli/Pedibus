package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Document(collection = "shift")
public class Shift {
    @Id
    private ObjectId turnID; //l'id del turno
    private ObjectId muleID; //id dell'accompagnatore
    private ObjectId AdminID; //id dell'admin
    private int lineaID; //id della linea
    private long date;//andata o ritorno
    private boolean direction; //se è settato True = andata , false se è ritorno


}
