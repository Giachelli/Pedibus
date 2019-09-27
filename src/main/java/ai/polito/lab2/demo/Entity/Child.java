package ai.polito.lab2.demo.Entity;

import ai.polito.lab2.demo.Dto.ChildDTO;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "child")
public class Child {
    @Id
    private ObjectId idChild;

    /*
    @DBRef
    private Family idFamily;
    */

    @DBRef
    private User genitore;

    private boolean present = false;
    private boolean booked = false;
    private String familyName;
    private String nameChild;

    public ChildDTO convertDTO() {
        return ChildDTO.builder()
                .idChild(this.getIdChild())
                .nameChild(this.getNameChild())
                .familyName(this.getFamilyName())
                .build();
    }
}

// TODO: compito è modificare Person VM in ChildVM e creare (spostando anche alcuni metodi il ChildCOntroller (che
// utilizza il service etc)
