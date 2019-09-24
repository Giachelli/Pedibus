package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ConfirmUserVM {
    //todo vedere annotazione password
    private String password;
    private String confirmPassword;
    private String family_name;

}
