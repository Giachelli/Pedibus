package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Role;
import ai.polito.lab2.demo.Entity.Route;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class modifyRoleUserVM {
    ArrayList<Integer> newAdminRoutes;
    ArrayList<Integer> newMuleRoutes;
}
