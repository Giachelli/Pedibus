package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Builder
@Data
public class UserVM {
    private String username;
    private String family_name;
    private ObjectId userID;
}
