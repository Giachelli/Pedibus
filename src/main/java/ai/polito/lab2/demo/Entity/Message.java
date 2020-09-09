package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Data
@Builder
@Document(collection = "message")
public class Message {
    @Id
    ObjectId messageID;
    ObjectId senderID;
    ObjectId receiverID;
    String action; //eg: Richiesta turno, Accettazione/Rifiuto turno, Richiesta bimbo
    private long date;
    ObjectId shiftID;
    ObjectId reservationID;
    ObjectId childID;
    String nameChild; //utizzato solo quando il bambino viene cancellato e quindi il child id non mi permette più di trovarlo
    String familyName;// utizzato solo quando il bambino viene cancellato e quindi il child id non mi permette più di trovarlo
    Boolean messageShiftRequest;  //indica che è un messaggio relativo alla richiesta/disponibilità del turno
    Boolean messageChildCreation; //indica che è un messaggio relativo alla creazione del bimbo
    Boolean messageChildPrenotation; //indica che è un messaggio relativo alla prenotazione del bimbo
    Boolean messageChildDelete; //indica che è un messaggio relativo alla cancellazione di un bimbo
    Boolean messageUpdateOtherUser; //indica che è un messaggio relativo alla definizione di un nuovo accompagnatore
    Boolean messageUpdateUser; //indica che è un messaggio relativo alla definizione di un nuovo admin
    Boolean messageShiftResponse;
    Boolean read;
    ArrayList<Integer> adminRoutes = new ArrayList<>();
    ArrayList<Integer> muleRoutes = new ArrayList<>();
    Integer route; // utilizzato solo quando ci sono other admin
    String status; // pending, accepted, rejected
    //String commento;
}
