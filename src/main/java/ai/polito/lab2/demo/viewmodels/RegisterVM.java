package ai.polito.lab2.demo.viewmodels;

import lombok.Data;

import javax.validation.constraints.Email;
import java.util.List;

// View Model per la Registrazione.

@Data
public class RegisterVM {
    //private int _id;
    @Email
    private String email;
    private List<String> role;

}
