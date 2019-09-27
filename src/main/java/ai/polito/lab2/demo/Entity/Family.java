package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "family")
public class Family {
    @Id
    ObjectId family_id;
    String family_name;
    @DBRef
    User user;
}
