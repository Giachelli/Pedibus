package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChildAllVM {
    private String childID;
    private String username;
    private String family_name;
    private String nameChild;
    private boolean isMale;
}
