package ai.polito.lab2.demo.Dto;

import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Family;
import ai.polito.lab2.demo.Entity.User;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ChildDTO {
    private ObjectId idChild;
    private String familyName;
    private String nameChild;



    public Child convert(User user) {
        return Child.builder()
                .nameChild(this.getNameChild())
                .familyName(this.getFamilyName())
                .idChild(this.getIdChild())
                .genitore(user)
                .build();
    }
}
