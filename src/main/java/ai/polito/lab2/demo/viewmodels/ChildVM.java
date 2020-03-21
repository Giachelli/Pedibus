package ai.polito.lab2.demo.viewmodels;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class ChildVM {

    private String childID;
    private String username;
    private String family_name;
    private String nameChild;
    private boolean isMale;
    private boolean booked;
    private String nomeLinea;
}

