package ai.polito.lab2.demo.Entity;

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
    private double lat;
    private double lng;

    @Override
    public String toString() {
        return this.getNome() + " " + this.get_id() + " " + getTime();
    }

}
