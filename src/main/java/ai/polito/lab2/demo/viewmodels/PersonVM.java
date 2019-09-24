package ai.polito.lab2.demo.viewmodels;

import lombok.Data;
import org.springframework.data.annotation.Id;

// View Model del bambino per passare ad angular le informazioni necessarie
@Data
public class PersonVM {

    private String nameA;
    @Id
    private String id;
    private boolean present = false;
    private boolean booked = false;

    public PersonVM(){

    }

   public PersonVM(String alunno){
        this.nameA = alunno;
    }

    public PersonVM(String id, String nameChild) {
        nameA = nameChild;
        this.id = id;


    }
}
