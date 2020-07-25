package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.RouteDTO;;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Repositories.StopRepo;
import ai.polito.lab2.demo.Entity.Route;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class RouteServiceImpl implements RouteService {


    @Autowired
    private RouteRepo routeRepo;

    @Autowired
    private StopRepo stopRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    private int idR;


    public int getIdR() {
        return idR;
    }

    public void setIdR(int idR) {
        this.idR = idR;
    }


    public List<Route> getAllRoutes() {
        return routeRepo.findAll();
    }

    public Route getRoutesByName(String NameR) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nameR").is(NameR));
        return mongoTemplate.find(query, Route.class).get(0);
    }

    @Override
    public Route getRoutesByID(int routeID) {

        return routeRepo.findRouteById(routeID);
    }

    @Override
    public RouteDTO getRoutesDTOByID(int routeID) {

        return routeRepo.findRouteById(routeID).convertToRouteDTO();
    }


    public void saveAll(ArrayList<Route> r) {

        for (Route route : r) {
            System.out.println(route.getNameR());
            if ((routeRepo.findRouteByNameR(route.getNameR()) == null)) {
                stopRepo.saveAll(route.getStopListA());
                stopRepo.saveAll(route.getStopListB());
                routeRepo.save(route);
                continue;
            } else {
                if (routeRepo.findRouteByNameR(route.getNameR()).getLastModified() != route.getLastModified()) {
                    stopRepo.saveAll(route.getStopListA());
                    stopRepo.saveAll(route.getStopListB());
                    routeRepo.save(route);
                }
            }

        }

    }

    @Override
    public RouteDTO findRouteByNameR(String nameR) {
        Route r = routeRepo.findRouteByNameR(nameR);
        this.setIdR(r.getId());
        return r.convertToRouteDTO();
    }

    @Override
    public int findIDRouteByNameR(String nameR) {
        return routeRepo.findRouteByNameR(nameR).getId();
    }

    @Override
    public void saveRoute(RouteDTO r) {

        routeRepo.save(r.convertToRoute(this.getIdR()));

    }

    @Override
    public void saveRoute(Route r) {
        routeRepo.save(r);
    }

    @Override
    public void readAll() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final File folder = new File("target/pedibus_routes");
        ArrayList<Route> routesArray = new ArrayList<>();

        for (final File file : Objects.requireNonNull(folder.listFiles())) {
            Route route = objectMapper.readValue(file, Route.class);
            route.setLastModified(file.lastModified());
            routesArray.add(route);
        }

        this.saveAll(routesArray);

    }

    @Override
    public List<String> getAccompagnaotori(int lineaID) {
        List<String> accompagnatori = new ArrayList<>();

        Route route = routeRepo.findRouteById(lineaID);

            // da verificare se questo accompagnatori si comporta bene
        accompagnatori=route.getUsernameAdmin();

        accompagnatori.addAll(route.getUsernameMule());

        accompagnatori.forEach((x) ->{
            System.out.println(x);
        });

        return accompagnatori;
    }

    /* @Override
    public Route getRouteByName(String nameR) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nameR").is(nameR));
        return mongoTemplate.findOne(query, Route.class);
    }*/


}
