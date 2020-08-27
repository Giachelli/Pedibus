package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@Data
@Builder
public class ShiftCreateVM { //classe che mappa l'oggetto che arriva dal frontEnd
    private String shiftId; //id del turno
    private String username; // username Mule
    private String usernameAdmin; //dell'admin
    private long data; //data del turno in millisecondi
    private int lineId; //id della linea
    private boolean direction; // per sapere se è andata (true) o ritorno (false)
    private String startShiftId; // id della fermata di partenza
    private String stopShiftId; // id della fermata di arrivo
    private String status; // tre valori: pending, accepted, rejected

    //funzione di controllo che va a vedere i campi della classe se sono nulli o poco sensati.
    public boolean control() {
        //long d = LocalDate.now().minusDays(1).toEpochDay();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if(this.username == null || this.usernameAdmin==null || (this.data <= cal.getTimeInMillis() -1)|| this.lineId < 0)
            return false;
        else return true;
    }
}
