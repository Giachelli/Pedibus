package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    Boolean read;
    String status; // pending, accepted, rejected
}
