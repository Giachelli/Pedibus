package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Role;
import ai.polito.lab2.demo.Entity.Route;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@Builder
public class modifyRoleUserVM {
    ArrayList<Integer> adminRoutes;
    ArrayList<Integer> muleRoutes;
    ArrayList<Boolean> availability;
    HashMap<Integer, ArrayList<ObjectId>> stopAndata;
    HashMap<Integer, ArrayList<ObjectId>> stopRitorno;
    String modifiedBy;
}
