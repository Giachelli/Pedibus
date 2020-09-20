package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
public class RecoverVM {
    private String pass;
    private String confpass;
}

