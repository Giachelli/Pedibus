package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ModifyTurnVM {

    private ObjectId adminID;
}
