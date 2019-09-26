package ai.polito.lab2.demo.Dto;

import ai.polito.lab2.demo.Entity.Child;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class ChildDTO {
    private ObjectId idChild;
    private String familyName;
    private String nameChild;


    public Child convert(ObjectId familyID) {
        return Child.builder()
                .nameChild(this.getNameChild())
                .familyName(this.getFamilyName())
                .idChild(this.getIdChild())
                .idFamily(familyID)
                .build();
    }
}
