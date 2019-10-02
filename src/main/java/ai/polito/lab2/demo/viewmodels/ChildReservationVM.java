package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

// View Model del bambino per passare ad angular le informazioni necessarie
@Data
@Builder
public class ChildReservationVM {

    private String name_child;
    private String name_family;
    private ObjectId childID;
    private boolean present = false;
    private boolean booked = false;

/*    public ChildReservationVM(){

    }

   public ChildReservationVM(String alunno){
        this.nameA = alunno;
    }

    public ChildReservationVM(String id, String nameChild) {
        nameA = nameChild;
        this.id = id;


    }*/
}
