package ai.polito.lab2.demo;

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
    ObjectId idChild;

    private String nameChild;
}
