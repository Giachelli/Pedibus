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
    private String color;
    private String stopID;
    private String stopName;
    private String nameRoute;
    private String direction;//può essere sia solo andata, sia solo ritorno, sia entrambi -> bisogna mapparla con i true false degli altri direction
    //private date forever //da vedè come fare
    private String nextCorsa;
    private String hourNextCorsa;


}

