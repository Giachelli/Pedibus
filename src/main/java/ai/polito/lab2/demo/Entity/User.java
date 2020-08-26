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

    private ArrayList<ObjectId> childsID;

    private ArrayList<Boolean> availability;
    // la key è l'id della linea, mentre l'array contiene gli id degli stop preferiti dal mule
    private HashMap<Integer, ArrayList<ObjectId>> andataStops;

    private HashMap<Integer, ArrayList<ObjectId>> ritornoStops;

    private Set<Integer> adminRoutesID;

    private Set<Integer> muleRoutesID;

    private String password;

    private String token; //per registrazione

    private String passtoken; // per recupero password

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
        if (!this.getRolesString().contains(userRole.getRole()))
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

    // TODO eliminare i duplicati se esistono

    public void addAdminRoutesID(ArrayList<Integer> adminRouteID) {
        if (this.adminRoutesID == null)
            this.adminRoutesID = new HashSet<>();
        this.adminRoutesID.addAll(adminRouteID);
    }

    public void addMuleRoutesID(ArrayList<Integer> muleRouteID) {
        if (this.muleRoutesID == null)
            this.muleRoutesID = new HashSet<>();
        this.muleRoutesID.addAll(muleRouteID);
    }

    public void removeRole(Role role_admin) {
        if (this.getRolesString().contains(role_admin.getRole()))
            this.roles.remove(role_admin);
    }

    public HashMap<Integer, ArrayList<String>> getUserVMMapStop (HashMap<Integer, ArrayList<ObjectId>> Stops){
        HashMap<Integer, ArrayList<String>> mappaReturned = new HashMap<>();

        for (Map.Entry<Integer, ArrayList<ObjectId>> entry : Stops.entrySet())
        {
            mappaReturned.put(entry.getKey(), convertObjectIdStop(entry.getValue()));
        }

        return mappaReturned;
    }

    public ArrayList<String> convertObjectIdStop (ArrayList<ObjectId> o){
        ArrayList<String> stops = new ArrayList<>();
        for (ObjectId objectId : o)
        {
            stops.add(o.toString());
        }
        return stops;
    }

}
