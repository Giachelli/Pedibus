package ai.polito.lab2.demo.viewmodels;

import lombok.Data;

import javax.validation.constraints.Email;

// View Model per la Registrazione.

@Data
public class RegisterVM {
    //private int _id;
    @Email
    private String email;
    private String role;

}
