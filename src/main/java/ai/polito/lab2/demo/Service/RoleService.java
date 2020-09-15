package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Role;

import java.util.ArrayList;
import java.util.List;

public interface RoleService {
    ArrayList<Role> convertRoles(List<String> roles) throws Exception;
}
