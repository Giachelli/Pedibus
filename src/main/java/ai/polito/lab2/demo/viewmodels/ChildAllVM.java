package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
@Builder
public class ChildAllVM {
    private String childID;
    private String username;
    private String family_name;
    @Min(3)
    private String nameChild;
    private boolean isMale;
}
