package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class MessageVM {
    String messageID;
    String shiftID;
    String reservationID;
    String nameChild;
    String familyName;
    String username;
    String text;
    String sender;
    Boolean messageShiftRequest;  //indica che è un messaggio relativo alla richiesta del turno
    Boolean messageChildCreation; //indica che è un messaggio relativo alla creazione del bimbo
    Boolean messageChildPrenotation; //indica che è un messaggio relativo alla prenotazione del bimbo (quella cliccata su calendario, quella singola)
    Boolean messageChildDelete; //indica che è un messaggio relativo alla cancellazione di un bimbo
    Boolean messageUpdateOtherUser; //indica agli admin di una linea che l'admin o un mule della medesima linea ha subito delle variazioni
    Boolean messageUpdateUser; //indica allo user che sono stati modificati i suoi privilegi
    ArrayList<String> adminRoutes = new ArrayList<>();
    ArrayList<String> muleRoutes = new ArrayList<>();
    Boolean messageShiftResponse; // indica il turno accettato dall'admin
    Boolean messageChildPlace; // indica il bambino che (prenotato o non prenotato) è stato preso in consegna
    Boolean messageNewUser;
    Boolean messageEditAvailability;
    Boolean messageDeleteTurn;
    Boolean read;
    private long date;
    String status;
    String dateShift;
    Boolean direction;
    String directionReservation; // potrebbe essere ridondante visto che c'è il boolean
    String nameLinea;
    String nameStop; //Salita
    String nameStopDiscesa;
    String oraFermata;
    String oraFermataDiscesa;
    String muleName; //per la cancellazione del turno
    boolean direzione; // usato per la direzione nel turno cancellato

}
