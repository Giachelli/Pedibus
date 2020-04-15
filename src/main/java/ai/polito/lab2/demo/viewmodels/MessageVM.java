package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageVM {
    String text;
    String sender;
    Boolean read;
    private long date;
    String status;
}
