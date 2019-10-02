package ai.polito.lab2.demo.Entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "reservation")
    @Data
    @Builder
    public class Reservation {
        @Id
        @ToString.Exclude
        private ObjectId id;

         private ObjectId childID;
         private ObjectId stopID;
         private int route;
         private String name_route;
         private String direction;
         private boolean booked;
         private boolean inPlace;
         private long date;
}



