package ai.polito.lab2.demo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
public class Person {

    private String nameA;
    @Id
    private String id;
    private boolean present = false;
    private boolean booked = false;

    public Person(){

    }

   public Person(String alunno){
        this.nameA = alunno;
    }

    public Person(String id, String nameChild) {
        nameA = nameChild;
        this.id = id;


    }
}
