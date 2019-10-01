package ai.polito.lab2.demo.Entity;


import ai.polito.lab2.demo.Dto.UserDTO;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Data
@Builder
@Document(collection = "user")
public class User implements UserDetails {

    @Id
    private ObjectId _id;

    private String username;

    private String family_name;

    private String password;

    private String token;  //capire se salva il token del jwt e se serve

   //private List<String> pass_token = new ArrayList<>();

    private String passtoken;

    private Date expiry_passToken;

    private Date expiryDate;

    private boolean isEnabled;

    @Builder.Default
    @DBRef
    private List<Role> roles = new ArrayList<>();


   @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map((Role role) -> new SimpleGrantedAuthority(role.getRole())).collect(toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

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

    public UserDTO convertToDTO() {
       return UserDTO.builder()
               .email(this.getUsername()).roles(this.getRoles())
               .family_name(this.getFamily_name())
               .token(this.getToken()).expiryDate(this.getExpiryDate()).build();
    }
}
