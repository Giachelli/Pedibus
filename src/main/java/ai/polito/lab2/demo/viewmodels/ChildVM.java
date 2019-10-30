package ai.polito.lab2.demo.viewmodels;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ChildVM {
    private String childID;
    private String username;
    private String name_family;
    private String nameChild;
    private boolean isMale;
}

