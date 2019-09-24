package ai.polito.lab2.demo;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "route")
@Data
public class Route {
    @Id
    private int id;
    private String nameR;
    private List<String> usernamesAdmin;
    private ArrayList<Stop> stopListA;
    private ArrayList<Stop> stopListB;
    private long lastModified;


    @Override
    public String toString(){
        return this.getNameR()+" "+ this.getId() ;
    }

    public void addAdmin(String username) {
        if (this.usernamesAdmin == null )
            this.usernamesAdmin = new ArrayList<>();
        this.usernamesAdmin.add(username);

    }
}
