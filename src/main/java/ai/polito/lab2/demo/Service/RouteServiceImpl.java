package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.ReadRoute;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Repositories.StopRepo;
import ai.polito.lab2.demo.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {


    @Autowired
    private RouteRepo routeRepo;

    @Autowired
    private StopRepo stopRepo;

    @Autowired
    private MongoTemplate mongoTemplate;


    public List<Route> getAllRoutes()  {
        Query query = new Query();
        query.fields().include("nameR").exclude("_id");
        return mongoTemplate.find(query,Route.class);
    }

    public Route getRoutesByName(String NameR) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nameR").is(NameR));
        return  mongoTemplate.find(query,Route.class).get(0);
    }


    ArrayList<Route> routes;

    public ArrayList<Route> PopulateDb () throws IOException {
        ReadRoute r = new ReadRoute();
        routes = r.readAll();

        for(int i=0;i<routes.size();i++){
            System.out.println(routes.get(i).toString());
            Route s = routes.get(i);
        }

        return routes;
    }

    public void save(ArrayList<Route> r) {

        for (Route route : r)
        {
            System.out.println(route.getNameR());
            if((routeRepo.findRouteByNameR(route.getNameR()) == null))
            {
                stopRepo.saveAll(route.getStopListA());
                stopRepo.saveAll(route.getStopListB());
                routeRepo.save(route);
                continue;
            }
            if (routeRepo.findRouteByNameR(route.getNameR()).getLastModified() != route.getLastModified())
            {
                stopRepo.saveAll(route.getStopListA());
                stopRepo.saveAll(route.getStopListB());
                routeRepo.save(route);
            }


        }

    }





   /* @Override
    public Route getRouteByName(String nameR) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nameR").is(nameR));
        return mongoTemplate.findOne(query, Route.class);
    }*/


}
