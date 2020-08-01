package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Route;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.ArrayList;

@Data
@Builder
public class UserRouteVM {
    private ObjectId userID;
    private ArrayList<Integer> adminRoute;
    private ArrayList<Integer> muleRoute;

}
