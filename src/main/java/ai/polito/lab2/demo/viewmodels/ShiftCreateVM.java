package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ShiftCreateVM { //classe che mappa l'oggetto che arriva dal frontEnd
    private ObjectId shiftId; //id del turno
    private String username; // username Mule
    private String usernameAdmin; //dell'admin
    private long data; //data del turno in millisecondi
    private int lineId; //id della linea
    private boolean direction; // per sapere se è andata (true) o ritorno (false)

    //funzione di controllo che va a vedere i campi della classe se sono nulli o poco sensati.
    public boolean control() {
        if(this.username == null || this.usernameAdmin==null || this.data > 1500 || this.lineId < 0)
            return false;
        else return true;
    }
}
