package ai.polito.lab2.demo.Dto;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ShiftDTO {
    private ObjectId muleID;
    private ObjectId adminID;
    private long data;
    private int lineId;
    private boolean direction;
    private String status;
}
