package ai.polito.lab2.demo.Entity;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "child")
public class Child {
    @Id
    private ObjectId childID;
    private String username; //email del genitore (user)
    private String family_name;
    private String nameChild;
    private boolean isMale;
    private String color;
    private String stopID; // utile per la fermata di default all'iscrizione del bambino
    private String nameRoute; // utile per la linea di default all'iscrizione del bambino (conviene passare l'id per avere più info)
    private String direction; //può essere sia solo andata, sia solo ritorno, sia entrambi -> bisogna mapparla con i true false degli altri direction
    //private date forever //da vedè come fare
/*
    public ChildDTO convertDTO() {
        return ChildDTO.builder()
                .childID(this.getChildID())
                .nameChild(this.getNameChild())
                .familyName(this.getFamilyName())
                .build();
    }*/
}

// TODO: compito è modificare Person VM in ChildVM e creare (spostando anche alcuni metodi il ChildCOntroller (che
// utilizza il service etc)
