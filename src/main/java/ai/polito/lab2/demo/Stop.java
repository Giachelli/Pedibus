package ai.polito.lab2.demo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "stop")
@Data
public class Stop {

    @Id
    private ObjectId _id;

    private String nome;
    private int num_s;
    private String time;
    //private int id_R;
    //private char Direzione ;//A= andata, e R=ritorno;

    @Override
    public String toString(){
        return this.getNome()+" "+ this.get_id()+" "+getTime() ;
    }

}
