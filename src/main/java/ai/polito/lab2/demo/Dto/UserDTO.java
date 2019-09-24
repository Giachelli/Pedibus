package ai.polito.lab2.demo.Dto;

import ai.polito.lab2.demo.Role;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
@Builder
public class UserDTO {
    @Email
    private String email;
    private List<Role> roles;
    private String token;  //capire se salva il token del jwt e se serve
    private Date expiryDate;

    public void addRole(Role userRole) {
        if(!this.getRolesString().contains(userRole.getRole()))
            this.roles.add(userRole);

    }

    public List<String> getRolesString() {
        return this.roles.stream().map((Role role) -> role.getRole()).collect(toList());
    }

    public Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

}
