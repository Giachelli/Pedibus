package ai.polito.lab2.demo.Dto;

import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.Stop;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class RouteDTO {
    private String nameR;
    private List<String> usernamesAdmin;
    private ArrayList<Stop> stopListA;
    private ArrayList<Stop> stopListB;
    private long lastModified;

    public void addAdmin(String username) {
        if (this.usernamesAdmin == null )
            this.usernamesAdmin = new ArrayList<>();
        this.usernamesAdmin.add(username);

    }

    @Override
    public String toString(){
        return this.getNameR();
    }

   public Route convertToRoute(int idR){
       /*  return Route.builder().nameR(this.getNameR()).id(idR)
                .stopListA(this.getStopListA())
                .stopListB(this.getStopListB())
                .usernamesAdmin(this.getUsernamesAdmin())
                .lastModified(this.getLastModified()).build();*/
       return new Route();
    }

}

