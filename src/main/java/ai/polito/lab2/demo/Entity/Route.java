package ai.polito.lab2.demo.Entity;


import ai.polito.lab2.demo.Dto.RouteDTO;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
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
    private List<String> usernameAdmin;
    private List<String> usernameMule;
    private ArrayList<Stop> stopListA;
    private ArrayList<Stop> stopListB;
    private long lastModified;


    @Override
    public String toString() {
        return this.getNameR() + " " + this.getId();
    }

    public RouteDTO convertToRouteDTO() {
        return RouteDTO.builder().nameR(this.getNameR())
                .stopListA(this.getStopListA())
                .stopListB(this.getStopListB())
                .usernamesAdmin(this.getUsernameAdmin())
                .usernameMule(this.getUsernameMule())
                .lastModified(this.getLastModified()).build();
    }

    public void addAdmin(String username) {
        if(this.usernameAdmin == null )
            this.usernameAdmin = new ArrayList<>();

        this.usernameAdmin.add(username);
    }

    public void removeAdmin (String username){
        if(this.usernameAdmin.contains(username))
            this.usernameAdmin.remove(username);
    }


    public void removeMule (String username){
        if(this.usernameMule.contains(username))
            this.usernameMule.remove(username);
    }


    public void addMule(String username) {
        if(this.usernameMule == null)
            this.usernameMule = new ArrayList<>();
        this.usernameMule.add(username);
    }
}
