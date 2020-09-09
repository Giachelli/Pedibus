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
    String text;
    String sender;
    Boolean messageShiftRequest;  //indica che è un messaggio relativo alla richiesta del turno
    Boolean messageChildCreation; //indica che è un messaggio relativo alla creazione del bimbo
    Boolean messageChildPrenotation; //indica che è un messaggio relativo alla prenotazione del bimbo
    Boolean messageChildDelete; //indica che è un messaggio relativo alla cancellazione di un bimbo
    Boolean messageUpdateOtherUser; //indica che è un messaggio relativo alla definizione di un nuovo accompagnatore
    Boolean messageUpdateUser; //indica che è un messaggio relativo alla definizione di un nuovo admin
    ArrayList<Integer> adminRoutes = new ArrayList<>();
    ArrayList<Integer> muleRoutes = new ArrayList<>();
    Boolean messageShiftResponse;
    Boolean read;
    private long date;
    String status;
    String dateShift;
    Boolean direction;
    String directionReservation;
    String nameLinea;
}
