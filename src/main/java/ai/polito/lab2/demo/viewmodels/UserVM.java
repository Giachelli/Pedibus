package ai.polito.lab2.demo.viewmodels;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserVM {
    private String username;
    private String family_name;
}
