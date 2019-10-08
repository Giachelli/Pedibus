package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Document(collection = "turn")
public class Turn {
    @Id
    private ObjectId turnID;

    private ObjectId muleID;
    private ObjectId partenzaStopID;
    private ObjectId arrivoStopID;
    private ObjectId confirmAdminID;
    private int lineaID;
    private long date;
    private boolean confirmed;
    // per ora è cosi: la prenotazione del turno è settata a TRUE se il turno è CONFERMATO
    // se il turno è rifiutato rimane nel DB come false = pending, rimane li per futuri utilizzi
    // ad esempio per qualche cambiamento che porta alla conferma del turno
    // se invece un UTENTE elimina/chiede l'eliminazione del turno, il turno suddetto viene eliminato dal DB
    // magari servirà aggiungere una variabile booleana ulteriore.
    private boolean direction; //se è settato True = andata , false se è ritorno


}
