package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Role;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Builder
@Data
public class UserVM {
    private String username;
    private String family_name;
    private String userID;
    private boolean isEnabled;
    //private List<Role> roles;
}
