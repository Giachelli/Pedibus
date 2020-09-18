package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.RouteDTO;;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Repositories.StopRepo;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.viewmodels.RouteVM;
import ai.polito.lab2.demo.viewmodels.StopVM;
import ai.polito.lab2.demo.viewmodels.UserVM;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.json.JsonParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RouteServiceImpl implements RouteService {


    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private UserService userService;

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
    public List<RouteVM> getAllRoutes() {
        String user = Principal.class.getName();
        List<Route> routes = routeRepo.findAll();
        ArrayList<RouteVM> routeVMs = new ArrayList<>();

        routes.forEach(route -> {
            ArrayList<StopVM> stopVMsA = new ArrayList<>();
            ArrayList<StopVM> stopVMsB = new ArrayList<>();

            route.getStopListA().forEach(stop -> {
                StopVM stopVM = StopVM.builder()
                        .stopID(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .nums(stop.getNums())
                        .lat(stop.getLat())
                        .lng(stop.getLng())
                        .build();

                stopVMsA.add(stopVM);
            });

            route.getStopListB().forEach(stop -> {
                StopVM stopVM = StopVM.builder()
                        .stopID(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .nums(stop.getNums())
                        .lat(stop.getLat())
                        .lng(stop.getLng())
                        .build();
                stopVMsB.add(stopVM);
            });
            List<UserVM> muleVMList = new ArrayList<>();
            List<UserVM> adminVMList = new ArrayList<>();

            if (route.getUsernameAdmin() != null)
                for (String u : route.getUsernameAdmin()) {
                    User admin = userService.getUserByUsername(u);
                    UserVM adminVM = UserVM.builder()
                            .userID(admin.get_id().toString())
                            .username(u)
                            .family_name(admin.getFamily_name())
                            .isEnabled(admin.isEnabled())
                            .build();
                    adminVMList.add(adminVM);
                }

            if (route.getUsernameMule() != null)
                for (String u : route.getUsernameMule()) {
                    User mule = userService.getUserByUsername(u);
                    UserVM muleVM = UserVM.builder()
                            .userID(mule.get_id().toString())
                            .username(u)
                            .family_name(mule.getFamily_name())
                            .isEnabled(mule.isEnabled())
                            .availabilityVM(mule.getAvailability())
                            .andataStop(mule.getUserVMMapStop(mule.getAndataStops()))
                            .ritornoStop(mule.getUserVMMapStop(mule.getRitornoStops()))
                            .build();
                    muleVMList.add(muleVM);
                }

            RouteVM r = RouteVM.builder()
                    .id(route.getId())
                    .nameR(route.getNameR())
                    .stopListA(stopVMsA)
                    .stopListB(stopVMsB)
                    .usernameAdmin(adminVMList)
                    .usernameMule(muleVMList)
                    .build();

            routeVMs.add(r);
        });

        return routeVMs;
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

    @Override
    public Route getRoutesByID(int routeID) {
        return routeRepo.findRouteById(routeID);
    }

    /**
     * ritorna la route in base all'id
     * @param routeID id della route
     * @return ritorna la route in base all'id
     */
    @Override
    public RouteVM getRoutesVMByID(int routeID) {

        Route route = routeRepo.findRouteById(routeID);
        ArrayList<StopVM> stopVMsA = new ArrayList<>();
        ArrayList<StopVM> stopVMsB = new ArrayList<>();

        route.getStopListA().forEach(stop -> {
            StopVM stopVM = StopVM.builder()
                    .stopID(stop.get_id().toString())
                    .nameStop(stop.getNome())
                    .time(stop.getTime())
                    .nums(stop.getNums())
                    .lat(stop.getLat())
                    .lng(stop.getLng())
                    .build();

            stopVMsA.add(stopVM);
        });

        route.getStopListB().forEach(stop -> {
            StopVM stopVM = StopVM.builder()
                    .stopID(stop.get_id().toString())
                    .nameStop(stop.getNome())
                    .time(stop.getTime())
                    .nums(stop.getNums())
                    .lat(stop.getLat())
                    .lng(stop.getLng())
                    .build();
            stopVMsB.add(stopVM);
        });
        List<UserVM> muleVMList = new ArrayList<>();
        List<UserVM> adminVMList = new ArrayList<>();

        if (route.getUsernameAdmin() != null)
            for (String u : route.getUsernameAdmin()) {
                User admin = userService.getUserByUsername(u);
                UserVM adminVM = UserVM.builder()
                        .userID(admin.get_id().toString())
                        .username(u)
                        .family_name(admin.getFamily_name())
                        .isEnabled(admin.isEnabled())
                        .build();
                adminVMList.add(adminVM);
            }

        if (route.getUsernameMule() != null)
            for (String u : route.getUsernameMule()) {
                User mule = userService.getUserByUsername(u);
                UserVM muleVM = UserVM.builder()
                        .userID(mule.get_id().toString())
                        .username(u)
                        .family_name(mule.getFamily_name())
                        .isEnabled(mule.isEnabled())
                        .availabilityVM(mule.getAvailability())
                        .andataStop(mule.getUserVMMapStop(mule.getAndataStops()))
                        .ritornoStop(mule.getUserVMMapStop(mule.getRitornoStops()))
                        .build();
                muleVMList.add(muleVM);
            }

        RouteVM r = RouteVM.builder()
                .id(route.getId())
                .nameR(route.getNameR())
                .stopListA(stopVMsA)
                .stopListB(stopVMsB)
                .usernameAdmin(adminVMList)
                .usernameMule(muleVMList)
                .build();

        return r;}

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
    public RouteVM readSingle(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Route route = objectMapper.readValue(file, Route.class);
            if (!controlRoute(route)) {
                String error = "Error in new route of file " + file.getName();
                throw new IOException(error);
            }

            User user = userService.getUserByUsername(route.getUsernameAdmin().get(0));
            if(!user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN")))
                user.addRole(roleRepository.findByRole("ROLE_ADMIN"));
            List<Stop> saved = stopRepo.saveAll(route.getStopListA());

            stopRepo.saveAll(route.getStopListB());
            route.setUsernameAdmin(new ArrayList<>());
            route.setUsernameMule(new ArrayList<>());
            route.getUsernameAdmin().add(route.getEmails());
            Date date= new Date();
            long time = date.getTime();
            route.setLastModified(time);
            routeRepo.save(route);

            ArrayList<StopVM> stopVMsA = new ArrayList<>();
            ArrayList<StopVM> stopVMsB = new ArrayList<>();
            List<UserVM> adminVMList = new ArrayList<>();
            List<UserVM> muleVMList = new ArrayList<>();

            route.getStopListA().forEach(stop -> {
                StopVM stopVM = StopVM.builder()
                        .stopID(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .nums(stop.getNums())
                        .lat(stop.getLat())
                        .lng(stop.getLng())
                        .build();

                stopVMsA.add(stopVM);
            });

            route.getStopListB().forEach(stop -> {
                StopVM stopVM = StopVM.builder()
                        .stopID(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .nums(stop.getNums())
                        .lat(stop.getLat())
                        .lng(stop.getLng())
                        .build();
                stopVMsB.add(stopVM);
            });

            if (route.getUsernameAdmin() != null)
                for (String u : route.getUsernameAdmin()) {
                    User admin = userService.getUserByUsername(u);
                    UserVM adminVM = UserVM.builder()
                            .userID(admin.get_id().toString())
                            .username(u)
                            .family_name(admin.getFamily_name())
                            .isEnabled(admin.isEnabled())
                            .build();
                    adminVMList.add(adminVM);
                }

            if (route.getUsernameMule() != null)
                for (String u : route.getUsernameMule()) {
                    User mule = userService.getUserByUsername(u);
                    UserVM muleVM = UserVM.builder()
                            .userID(mule.get_id().toString())
                            .username(u)
                            .family_name(mule.getFamily_name())
                            .isEnabled(mule.isEnabled())
                            .availabilityVM(mule.getAvailability())
                            .andataStop(mule.getUserVMMapStop(mule.getAndataStops()))
                            .ritornoStop(mule.getUserVMMapStop(mule.getRitornoStops()))
                            .build();
                    muleVMList.add(muleVM);
                }

            return RouteVM.builder()
                    .id(route.getId())
                    .nameR(route.getNameR())
                    .stopListA(stopVMsA)
                    .stopListB(stopVMsB)
                    .usernameAdmin(adminVMList)
                    .usernameMule(muleVMList)
                    .build();


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
