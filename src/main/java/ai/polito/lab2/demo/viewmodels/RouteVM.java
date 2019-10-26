package ai.polito.lab2.demo.viewmodels;

import ai.polito.lab2.demo.Entity.Stop;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// classe passata ad angular con le info necessarie
@Data
@Builder
public class RouteVM {

    private int id;
    private String nameR;
    private List<String> usernameAdmin;
    private List<String> usernameMule;
    private ArrayList<StopVM> stopListA;
    private ArrayList<StopVM> stopListB;
}
