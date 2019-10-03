package ai.polito.lab2.demo.viewmodels;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class RecoverVM {

    @NotEmpty
    @Size(min = 8, max = 32)
    private String pass;
    @NotEmpty
    @Size(min = 8, max = 32)
    private String confpass;

}

