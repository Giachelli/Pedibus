package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnCreateVM {
    private String username;
    private long data;
    private int lineId;
    private boolean direction;

}
