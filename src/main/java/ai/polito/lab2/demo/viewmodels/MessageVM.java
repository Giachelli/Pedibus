package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageVM {
    String messageID;
    String shiftID;
    String text;
    String sender;
    Boolean read;
    private long date;
    String status;
    String dateShift;
    Boolean direction;
    String nameLinea;
}
