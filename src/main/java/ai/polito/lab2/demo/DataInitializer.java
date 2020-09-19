package ai.polito.lab2.demo;


import ai.polito.lab2.demo.Entity.Role;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.MessageRepo;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.ReservationService;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.Service.StopService;
import ai.polito.lab2.demo.controllers.ChildController;
import ai.polito.lab2.demo.controllers.RouteController;
import ai.polito.lab2.demo.viewmodels.ChildVM;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepo users;

    @Autowired
    private RouteController routeController;

    @Autowired
    private RouteService routeService;

    @Autowired
    private StopService stopService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private ChildRepo childRepo;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ChildController childController;

    @Autowired
    private MessageRepo messageRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Entro nel run");
        reservationService.setFirstDay("15/09/2020");
        boolean addRouteId = true;
        Set<Integer> routeId = new HashSet<>();
        routeId.add(1);
        routeId.add(2);
        routeId.add(3);
        routeId.add(4);
        routeId.add(6);
        setRoleDb();
        if (users.findByUsername("admin@info.it") != null) {
            addRouteId = false;
        } else {
            this.users.save(User.builder()
                    .username("admin@info.it")
                    .family_name("head of school")
                    .password(this.passwordEncoder.encode("12345@dmin"))
                    .roles(Arrays.asList(roleRepository.findByRole("ROLE_USER"),
                            roleRepository.findByRole("ROLE_SYSTEM_ADMIN"),
                            roleRepository.findByRole("ROLE_MULE")))
                    .isEnabled(true)
                    .build()
            );
            System.out.println("Siamo sopra al populateDB");
            routeController.PopulateDb();
            if(addRouteId)
            {
                User u = users.findByUsername("admin@info.it");
                u.setAdminRoutesID(routeId);
                users.save(u);
            }

            setMule_Admin_Test();
            insertUserIntoDB(routeId);
            //insertChildIntoDB();
            //  insertMessageDb();
        }
/*
        log.debug("printing all users...");
        this.users.findAll().forEach(v -> log.debug(" User :" + v.toString()));*/
    }

    //funzione semplice che verrà cancellata ma che serve per testare
    private void setMule_Admin_Test() {
        Route r1 = routeService.getRoutesByID(1);
        Route r2 = routeService.getRoutesByID(2);


        r1.setUsernameAdmin(Arrays.asList("admin@info.it"));
        r2.setUsernameAdmin(Arrays.asList("admin@info.it"));
        r1.setUsernameMule(Arrays.asList("user1@info.it", "user2@info.it"));
        r2.setUsernameMule(Arrays.asList("user1@info.it", "user2@info.it"));
        routeService.saveRoute(r2);
        routeService.saveRoute(r1);

        r1 = routeService.getRoutesByID(3);
        r2 = routeService.getRoutesByID(4);

        r1.setUsernameAdmin(Arrays.asList("admin@info.it"));
        r2.setUsernameAdmin(Arrays.asList("admin@info.it"));
        r1.setUsernameMule(Arrays.asList("user1@info.it", "user2@info.it"));
        r2.setUsernameMule(Arrays.asList("user1@info.it", "user2@info.it"));
        routeService.saveRoute(r2);
        routeService.saveRoute(r1);


        r1 = routeService.getRoutesByID(6);
        r1.setUsernameAdmin(Arrays.asList("admin@info.it"));
        r1.setUsernameMule(Arrays.asList("user1@info.it", "user2@info.it"));
        routeService.saveRoute(r1);
    }

  /*  private void insertChildIntoDB() {
        System.out.println("creano il child");
        Route r1 = routeService.getRoutesByID(2);
        Route r2 = routeService.getRoutesByID(3);
        ArrayList<String> bubba = new ArrayList<>();
        bubba.add(r1.getStopListA().get(1).get_id().toString());
        bubba.add(r1.getStopListB().get(4).get_id().toString());
        ArrayList<String> bubba1 = new ArrayList<>();
        bubba1.add(r1.getStopListA().get(1).getNome());
        bubba1.add(r1.getStopListB().get(4).getNome());
        ArrayList<String> bubba2 = new ArrayList<>();
        bubba2.add(r1.getNameR());
        bubba2.add(r1.getNameR());
        ArrayList<String> bubba_ = new ArrayList<>();
        bubba_.add("");
        bubba_.add(r1.getStopListB().get(4).get_id().toString());
        ArrayList<String> bubba_1 = new ArrayList<>();
        bubba_1.add("");
        bubba_1.add(r1.getStopListB().get(4).getNome());
        ArrayList<String> bubba_2 = new ArrayList<>();
        bubba_2.add("");
        bubba_2.add(r1.getNameR());
       /*
        this.childController.registerChild(
                ChildVM.builder()
                        .nameChild("Luigi")
                        .username("user1@info.it")
                        .family_name("Malnati")
                        .color("#B0C4DE")
                        .isMale(true)
                        .stopID(bubba)
                        .stopName(bubba1)
                        .nameRoute(bubba2)
                        .direction("entrambi")
                        .build()
        );
        this.childController.registerChild(
                ChildVM.builder()
                        .nameChild("Mario")
                        .username("user2@info.it")
                        .family_name("Servetti")
                        .color("#F4D8CD")
                        .isMale(true)
                        .stopID(bubba_)
                        .stopName(bubba_1)
                        .nameRoute(bubba_2)
                        .direction("andata")
                        .build()
        );
        bubba_ = new ArrayList<>();
        bubba_.add("");
        bubba_.add(r2.getStopListB().get(4).get_id().toString());
        bubba_1 = new ArrayList<>();
        bubba_1.add("");
        bubba_1.add(r2.getStopListB().get(4).getNome());
        bubba_2 = new ArrayList<>();
        bubba_2.add("");
        bubba_2.add(r2.getNameR());

        this.childController.registerChild(
                ChildVM.builder()
                        .nameChild("Carla")
                        .username("user1@info.it")
                        .family_name("Malnati")
                        .color("#2F4F4F")
                        .isMale(true)
                        .stopID(bubba_)
                        .stopName(bubba_1)
                        .nameRoute(bubba_2)
                        .direction("andata")
                        .build()
        );
      /*  this.childController.registerChild(
                ChildVM.builder()
                        .nameChild("Alice")
                        .username("user2@info.it")
                        .family_name("Servetti")
                        .color("#D2691E")
                        .isMale(true)
                        .stopID(r1.getStopListA().get(1).get_id().toString())
                        .stopName(r1.getStopListA().get(1).getNome())
                        .nameRoute(r1.getNameR())
                        .direction("andata")
                        .build()
        );


    }*/


    public void insertUserIntoDB(Set<Integer> routeId) {

        Role role = this.roleRepository.findByRole("ROLE_USER");

        ArrayList<Boolean> disp = new ArrayList<>();
        disp.add(true);
        disp.add(true);
        disp.add(false);
        disp.add(false);
        disp.add(true);
        disp.add(false);
        disp.add(false);

        HashMap<Integer, ArrayList<ObjectId>> andata = new HashMap<>();
        HashMap<Integer, ArrayList<ObjectId>> ritorno = new HashMap<>();

        Stop s1 = stopService.findStopbyNameAndNumS("Incrocio Corso Stati Uniti-Statua", 4);
        Stop s2 = stopService.findStopbyNameAndNumS("Mixto", 2);
        ArrayList<ObjectId> andataS = new ArrayList<>();
        ArrayList<ObjectId> ritornoS = new ArrayList<>();
        andataS.add(s1.get_id());
        ritornoS.add(s2.get_id());
        andataS.add(stopService.findStopbyNameAndNumS("Mixto", 5).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Incrocio Corso Stati Uniti-Statua", 3).get_id());
        andata.put(6, andataS);
        ritorno.put(6, ritornoS);

        andataS = new ArrayList<>();
        ritornoS = new ArrayList<>();
        andataS.add(stopService.findStopbyNameAndNumS("Corso Germano Sommeiller 39- Unicredit", 1).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Corso Duca degli Abruzzi 28", 2).get_id());
        andataS.add(stopService.findStopbyNameAndNumS("Corso Duca degli Abruzzi 28", 5).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Corso Germano Sommeiller 39- Unicredit", 6).get_id());
        andata.put(4, andataS);
        ritorno.put(4, ritornoS);

        andataS = new ArrayList<>();
        ritornoS = new ArrayList<>();
        andataS.add(stopService.findStopbyNameAndNumS("Piazza Sabotino 38- Unicredit", 2).get_id());
        andataS.add(stopService.findStopbyNameAndNumS("Corso Castelfidardo- Fermata 3281", 5).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Corso Castelfidardo- Fermata 3281", 2).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Piazza Sabotino 38- Unicredit", 5).get_id());
        andata.put(1, andataS);
        ritorno.put(1, ritornoS);

        andataS = new ArrayList<>();
        ritornoS = new ArrayList<>();
        andataS.add(stopService.findStopbyNameAndNumS("Corso Mediterraneo 124- Fermata 3550", 4).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Corso Mediterraneo- Incrocio Corso Peschiera", 2).get_id());
        andataS.add(stopService.findStopbyNameAndNumS("Corso Mediterraneo- Incrocio Corso Peschiera", 5).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Corso Mediterraneo 124- Fermata 3550", 3).get_id());
        andata.put(2, andataS);
        ritorno.put(2, ritornoS);

        andataS = new ArrayList<>();
        ritornoS = new ArrayList<>();
        andataS.add(stopService.findStopbyNameAndNumS("Via Paolo Sacchi 18- Unicredit", 1).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("corso Stati Uniti- Incrocio Corso Duca (stazione tobike)", 2).get_id());
        andataS.add(stopService.findStopbyNameAndNumS("corso Stati Uniti- Incrocio Corso Duca (stazione tobike)", 5).get_id());
        ritornoS.add(stopService.findStopbyNameAndNumS("Via Paolo Sacchi 18- Unicredit", 6).get_id());
        andata.put(3, andataS);
        ritorno.put(3, ritornoS);

        this.users.save(User.builder()
                .username("user1@info.it")
                .family_name("Malnati")
                .password(this.passwordEncoder.encode("1user@user"))
                .roles(Arrays.asList(role, roleRepository.findByRole("ROLE_MULE")))
                .availability(disp)
                .andataStops(andata)
                .ritornoStops(ritorno)
                .muleRoutesID(routeId)
                //.roles(Arrays.asList(role))
                .isEnabled(true)
                .build()
        );

        this.users.save(User.builder()
                .username("giacomo.chelli4@gmail.com")
                .family_name("Chelli")
                .password(this.passwordEncoder.encode("1user@user"))
                .roles(Arrays.asList(role, roleRepository.findByRole("ROLE_MULE")))
                .availability(disp)
                .andataStops(andata)
                .ritornoStops(ritorno)
                .muleRoutesID(routeId)
                //.roles(Arrays.asList(role))
                .isEnabled(true)
                .build()
        );


        this.users.save(User.builder()
                .username("user2@info.it")
                .family_name("Servetti")
                .password(this.passwordEncoder.encode("2user@user"))
                //.roles(Arrays.asList(role))
                .roles(Arrays.asList(role, roleRepository.findByRole("ROLE_MULE")))
                .muleRoutesID(routeId)
                .availability(disp)
                .andataStops(andata)
                .ritornoStops(ritorno)
                .isEnabled(true)
                .build()
        );


        this.users.save(User.builder()
                .username("user3@info.it")
                .family_name("Cabodi")
                .password(this.passwordEncoder.encode("3user@user"))
                .roles(Arrays.asList(role))
                .isEnabled(true)
                .build()
        );


        this.users.save(User.builder()
                .username("user4@info.it")
                .family_name("Lioy")
                .password(this.passwordEncoder.encode("1user@user"))
                .roles(Arrays.asList(role))
                .isEnabled(true)
                .build()
        );


        this.users.save(User.builder()
                .username("user5@info.it")
                .family_name("Bernardi")
                .password(this.passwordEncoder.encode("5user@user"))
                .roles(Arrays.asList(role))
                .isEnabled(true)
                .build()
        );


        this.users.save(User.builder()
                .username("user6@info.it")
                .family_name("Sanchez")
                .password(this.passwordEncoder.encode("6user@user"))
                .roles(Arrays.asList(role))
                .isEnabled(true)
                .build()
        );
    }


    private void setRoleDb() {
        Role adminSystemRole = roleRepository.findByRole("ROLE_SYSTEM_ADMIN");
        if (adminSystemRole == null) {
            Role newAdminRole = new Role();
            newAdminRole.setRole("ROLE_SYSTEM_ADMIN");
            roleRepository.save(newAdminRole);
        }
        Role adminRole = roleRepository.findByRole("ROLE_ADMIN");
        if (adminRole == null) {
            Role newAdminRole = new Role();
            newAdminRole.setRole("ROLE_ADMIN");
            roleRepository.save(newAdminRole);
        }

        Role userRole = roleRepository.findByRole("ROLE_USER");
        ;
        if (userRole == null) {
            Role newUserRole = new Role();
            newUserRole.setRole("ROLE_USER");
            roleRepository.save(newUserRole);
        }
        Role muleRole = roleRepository.findByRole("ROLE_MULE"); // mule ---> accompagnatore
        if (muleRole == null) {
            Role newMuleRole = new Role();
            newMuleRole.setRole("ROLE_MULE");
            roleRepository.save(newMuleRole);
        }
    }
    /*
    private void insertMessageDb() {
        System.out.println("Provo a creare il message");
        this.messageRepo.save(
                Message.builder()
                        .senderID(users.findByUsername("user1@info.it").get_id())
                        .action("Ho modificato il turno")
                        .receiverID(users.findByUsername("user2@info.it").get_id())
                        .date(new Date().getTime())
                        .read(false)
                        .build()
        );
    }
    */


}



