package ai.polito.lab2.demo.viewmodels;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

@Data
@Builder
public class ChildVM {

    private String childID;
    private String username;
    private String family_name;
    private String nameChild;
    private boolean isMale;
    private String color;
    private ArrayList<String> stopID;
    private ArrayList<String> stopName; // ["stop_andata", "stop_ritorno"]; nel caso in cui uno non sia selezionato, stringa vuota
    private ArrayList<String> nameRoute;
    private String direction;//può essere sia solo andata, sia solo ritorno, sia entrambi -> bisogna mapparla con i true false degli altri direction
    //private date forever //da vedè come fare
}

