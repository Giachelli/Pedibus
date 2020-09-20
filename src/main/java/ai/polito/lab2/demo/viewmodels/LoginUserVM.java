package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class LoginUserVM {
    private String username;
    private String userID;
    private String token;
    private String family_name;
    private List<String> roles;
    private Set<Integer> adminRoutes;
    private Set<Integer> adminMules;
    private int childsNumber;
    private HashMap<Integer, ArrayList<String>> andataStop;
    private HashMap<Integer, ArrayList<String>> ritornoStop;
}
