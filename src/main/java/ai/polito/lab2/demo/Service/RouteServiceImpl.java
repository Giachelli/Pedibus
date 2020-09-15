package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.RouteDTO;;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Repositories.StopRepo;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.UserRepo;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.jni.Time;
import org.bson.json.JsonParseException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.util.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RouteServiceImpl implements RouteService {


    @Autowired
    private RouteRepo routeRepo;

    @Autowired
    private UserRepo userRepo;

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


    /**
     *
     * @return ritorna tutte le route
     */
    public List<Route> getAllRoutes() {
        return routeRepo.findAll();
    }

    /**
     *
     * @param NameR nome della route
     * @return ritorna la route in base al nome
     */
    public Route getRoutesByName(String NameR) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nameR").is(NameR));
        return mongoTemplate.find(query, Route.class).get(0);
    }

    /**
     * ritorna la route in base all'id
     * @param routeID id della route
     * @return ritorna la route in base all'id
     */
    @Override
    public Route getRoutesByID(int routeID) {

        return routeRepo.findRouteById(routeID);
    }

    /**
     * ritorna la routeDto in base all'id
     * @param routeID id della route
     * @return ritorna la routeDTO in base all'id
     */
    @Override
    public RouteDTO getRoutesDTOByID(int routeID) {

        return routeRepo.findRouteById(routeID).convertToRouteDTO();
    }


    /**
     * salva tutte le linee passate nell'array list
     * @param r arraylist con tutte le linee da salvare (fase di configurazione)
     */
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

    /**
     *
     * @param nameR nome della route
     * @return ritorna la route in base al nome
     */
    @Override
    public RouteDTO findRouteByNameR(String nameR) {
        Route r = routeRepo.findRouteByNameR(nameR);
        this.setIdR(r.getId());
        return r.convertToRouteDTO();
    }

    /**
     * ritorna l'id dal nome della route
     * @param nameR nome della route
     * @return id della route con quel nome
     */
    @Override
    public int findIDRouteByNameR(String nameR) {
        return routeRepo.findRouteByNameR(nameR).getId();
    }

    @Override
    public void saveRoute(RouteDTO r) {

        routeRepo.save(r.convertToRoute(this.getIdR()));

    }

    /**
     * Salvataggio di una route
     * @param r
     */
    @Override
    public void saveRoute(Route r) {
        routeRepo.save(r);
    }

    /**
     * Lettura da file delle route utilizzata in fase di configurazione
     * @throws IOException
     */
    @Override
    public void readAll() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final File folder = ResourceUtils.getFile("classpath:pedibus_routes//");
        ArrayList<Route> routesArray = new ArrayList<>();

        for (final File file : Objects.requireNonNull(folder.listFiles())) {
            Route route = objectMapper.readValue(file, Route.class);
            if (userRepo.findByUsername(route.getEmails()) == null) {
                String error = "ERROR IN EMAILS of file " + file.getName();
                throw new IOException(error);
            }
            route.setLastModified(file.lastModified());
            routesArray.add(route);
        }

        this.saveAll(routesArray);

    }

    /**
     * Lettura di una singola route da file per caricamento
     * @param file che contiene la route da controllare e caricare
     * @return ritorna la route creata se tutto va bene
     * @throws IOException errore nel parsing del json
     */
    @Override
    public Route readSingle(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Route route = objectMapper.readValue(file, Route.class);
            if (!controlRoute(route)) {
                String error = "Error in new route of file " + file.getName();
                throw new IOException(error);
            }
            List<Stop> saved = stopRepo.saveAll(route.getStopListA());

            stopRepo.saveAll(route.getStopListB());
            route.setUsernameAdmin(new ArrayList<>());
            route.setUsernameMule(new ArrayList<>());
            route.getUsernameAdmin().add(route.getEmails());
            Date date= new Date();
            long time = date.getTime();
            route.setLastModified(time);
            routeRepo.save(route);
            return route;

        } catch (JsonMappingException | JsonParseException jsonException) {
            throw new IOException("errore nel parsing json");
        }

    }

    /**
     * Funzione che controlla la route passata se ha tutti i campi corretti
     * @param route
     * @return true se tutto ok
     */
    private boolean controlRoute(Route route) {
        if (routeRepo.findRouteByNameR(route.getNameR()) != null)
            return false;
        if (routeRepo.findRouteById(route.getId()) != null)
            return false;
        if (userRepo.findByUsername(route.getEmails()) == null)
            return false;
        if (route.getStopListA() == null || route.getStopListA().size() == 0 || route.getStopListB() == null || route.getStopListB().size() == 0)
            return false;

        for (int i = 0; i < route.getStopListA().size(); i++) {
            if (route.getStopListA().get(i).getTime() == "" ||
                    route.getStopListA().get(i).getLat() < 0 ||
                    route.getStopListA().get(i).getLng() < 0 ||
                    route.getStopListA().get(i).getNome() == "" ||
                    route.getStopListA().get(i).getNums() < 0)
                return false;
        }


        for (int i = 0; i < route.getStopListB().size(); i++) {
            if (route.getStopListB().get(i).getTime() == "" ||
                    route.getStopListB().get(i).getLat() < 0 ||
                    route.getStopListB().get(i).getLng() < 0 ||
                    route.getStopListB().get(i).getNome() == "" ||
                    route.getStopListB().get(i).getNums() < 0)
                return false;
        }

        return true;
    }

    /**
     * ritorna gli username dei mule per la linea
     * @param lineaID id della linea
     * @return ritorna la lista degli username dei mule per la linea
     */
    @Override
    public List<String> getAccompagnaotori(int lineaID) {
        List<String> accompagnatori = new ArrayList<>();

        Route route = routeRepo.findRouteById(lineaID);

        // da verificare se questo accompagnatori si comporta bene
        accompagnatori = route.getUsernameAdmin();

        accompagnatori.addAll(route.getUsernameMule());

        accompagnatori.forEach((x) -> {
            System.out.println(x);
        });

        return accompagnatori;
    }

}
