package ai.polito.lab2.demo.Dto;

import ai.polito.lab2.demo.Entity.Child;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ChildDTO {
    private ObjectId childID;
    private String familyName;
    private String nameChild;


    public Child convert(ObjectId userID) {
        return Child.builder()
                .nameChild(this.getNameChild())
                .family_name(this.getFamilyName())
                .childID(this.getChildID())
                .username("bubba")
                .build();
    }
}
