package ai.polito.lab2.demo.viewmodels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
public class AuthenticationRequestVM {
    private String username;
    private String password;
}