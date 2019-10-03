package ai.polito.lab2.demo.Repositories;

import ai.polito.lab2.demo.Entity.Route;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepo extends MongoRepository<Route, Integer> {

    Route findRouteByNameR(String nameR);

    Route findRouteById(int i);


}
