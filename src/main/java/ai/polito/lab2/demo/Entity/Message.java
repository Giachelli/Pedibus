package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

/**
 * Entity del messaggio
 */
@Data
@Builder
@Document(collection = "message")
public class Message {
    @Id
    ObjectId messageID;
    ObjectId senderID; //id del sender
    ObjectId receiverID; // id del receiver
    String action; //eg: Richiesta turno, Accettazione/Rifiuto turno, Richiesta bimbo
    private long date; //time dell'invio del messaggio
    private long dateTurns; //per il messaggio di cancellazione di un turno
    ObjectId shiftID; // id dello turno
    Integer route; // utilizzato solo quando ci sono other admin e cancellazione turno
    ObjectId startID; // id della fermata di partenza, utilizzata per i messaggi che riguardano le prenotazioni e i turni
    ObjectId stopID; // id della fermata di arrivo, utilizzata per i messaggi che riguardano le prenotazioni e i turni
    boolean direzione; // direzione della prenotazione
    String muleName; // utilizzato in caso di cancellazione prenotazione turno
    ObjectId reservationID; // id della prenotazione, utilizzato per i messaggi che riguardano le prenotazioni
    ObjectId childID; // id del child, utilizzato per messaggio creazione bimbo
    ObjectId userID; //id dello user, utilizzato per avvertire quando lo user viene creato
    String nameChild; //utizzato solo quando il bambino viene cancellato e quindi il child id non mi permette più di trovarlo
    String familyName;// utizzato solo quando il bambino viene cancellato e quindi il child id non mi permette più di trovarlo
    Boolean messageShiftRequest;  //indica che è un messaggio relativo alla richiesta/disponibilità del turno
    Boolean messageChildCreation; //indica che è un messaggio relativo alla creazione del bimbo
    Boolean messageChildPrenotation; //indica che è un messaggio relativo alla prenotazione del bimbo
    Boolean messageChildDelete; //indica che è un messaggio relativo alla cancellazione di un bimbo
    Boolean messageUpdateOtherUser; //indica che è un messaggio relativo alla definizione di un nuovo accompagnatore
    Boolean messageUpdateUser; //indica che è un messaggio relativo alla definizione di un nuovo admin
    Boolean messageShiftResponse;
    Boolean messageChildPlace;
    Boolean messageNewUser; // indica che un nuovo user è stato iscritto a sistena
    Boolean messageEditAvailability; //messaggio che ricevono gli user quando un mule cambia le sue disponibilità
    Boolean messageDeleteTurn; // messaggio di eliminazione turno
    Boolean read;
    ArrayList<Integer> adminRoutes = new ArrayList<>();
    ArrayList<Integer> muleRoutes = new ArrayList<>();
    String status; // pending, accepted, rejected
    //String commento;
}
