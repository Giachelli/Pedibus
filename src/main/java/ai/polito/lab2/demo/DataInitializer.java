package ai.polito.lab2.demo;


import ai.polito.lab2.demo.Entity.Role;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.ChildService;
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
    ChildService childService;

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
    private ReservationService reservationService;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("Entro nel run");
        reservationService.setFirstDay("15/09/2020");
        boolean addRouteId = true;
        Set<Integer> routeId = new HashSet<>();
        routeId.add(1);
        routeId.add(2);
        setRoleDb();
        if (users.findByUsername("giacomo.chelli4@gmail.com") != null) {
            addRouteId = false;
        } else {
            ArrayList<Boolean> disp = new ArrayList<>();
            disp.add(false);
            disp.add(true);
            disp.add(true);
            disp.add(true);
            disp.add(true);
            disp.add(true);
            disp.add(false);

            HashMap<Integer, ArrayList<ObjectId>> andata = new HashMap<>();
            HashMap<Integer, ArrayList<ObjectId>> ritorno = new HashMap<>();
            ArrayList<ObjectId> andataS = new ArrayList<>();
            ArrayList<ObjectId> ritornoS = new ArrayList<>();
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

            Stop s1 = stopService.findStopbyNameAndNumS("Incrocio Corso Stati Uniti-Statua", 4);
            Stop s2 = stopService.findStopbyNameAndNumS("Mixto", 2);
            andataS = new ArrayList<>();
            ritornoS = new ArrayList<>();
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
            andataS.add(stopService.findStopbyNameAndNumS("Via Paolo Sacchi 18- Unicredit", 1).get_id());
            ritornoS.add(stopService.findStopbyNameAndNumS("corso Stati Uniti- Incrocio Corso Duca (stazione tobike)", 2).get_id());
            andataS.add(stopService.findStopbyNameAndNumS("corso Stati Uniti- Incrocio Corso Duca (stazione tobike)", 5).get_id());
            ritornoS.add(stopService.findStopbyNameAndNumS("Via Paolo Sacchi 18- Unicredit", 6).get_id());
            andata.put(3, andataS);
            ritorno.put(3, ritornoS);


            this.users.save(User.builder()
                    .username("giacomo.chelli4@gmail.com")
                    .family_name("head of school")
                    .password(this.passwordEncoder.encode("12345@Admin"))
                    .roles(Arrays.asList(roleRepository.findByRole("ROLE_USER"),
                            roleRepository.findByRole("ROLE_SYSTEM_ADMIN"),
                            roleRepository.findByRole("ROLE_MULE"),
                            roleRepository.findByRole("ROLE_ADMIN")))
                    .availability(disp)
                    .andataStops(andata)
                    .ritornoStops(ritorno)
                    .isEnabled(true)
                    .build()
            );
            routeController.PopulateDb();
            if(addRouteId)
            {
                User u = users.findByUsername("giacomo.chelli4@gmail.com");
                u.setAdminRoutesID(routeId);
                u.setMuleRoutesID(routeId);
                setMule_Admin_Test();
                users.save(u);
            }

            insertUserIntoDB(routeId);
            insertChildIntoDB();
        }
    }

    //funzione semplice che verrà cancellata ma che serve per testare
    private void setMule_Admin_Test() {
        Route r1 = routeService.getRoutesByID(1);
        Route r2 = routeService.getRoutesByID(2);


        r1.setUsernameAdmin(Arrays.asList("giacomo.chelli4@gmail.com"));
        r2.setUsernameAdmin(Arrays.asList("giacomo.chelli4@gmail.com"));
        r1.setUsernameMule(Arrays.asList("giacomo.chelli4@gmail.com"));
        r2.setUsernameMule(Arrays.asList("giacomo.chelli4@gmail.com"));
        routeService.saveRoute(r2);
        routeService.saveRoute(r1);

        r1 = routeService.getRoutesByID(3);
        r2 = routeService.getRoutesByID(4);

        r1.setUsernameAdmin(Arrays.asList("giacomo.chelli4@gmail.com"));
        r2.setUsernameAdmin(Arrays.asList("giacomo.chelli4@gmail.com"));
        r1.setUsernameMule(Arrays.asList("giacomo.chelli4@gmail.com"));
        r2.setUsernameMule(Arrays.asList("giacomo.chelli4@gmail.com"));
        routeService.saveRoute(r2);
        routeService.saveRoute(r1);


        r1 = routeService.getRoutesByID(6);
        r1.setUsernameAdmin(Arrays.asList("giacomo.chelli4@gmail.com"));
        r1.setUsernameMule(Arrays.asList("giacomo.chelli4@gmail.com"));
        routeService.saveRoute(r1);
    }

    private void insertChildIntoDB() {
        Route r1 = routeService.getRoutesByID(2);
        ArrayList<String> bubba = new ArrayList<>();
        bubba.add(r1.getStopListA().get(1).get_id().toString());
        bubba.add(r1.getStopListB().get(4).get_id().toString());
        ArrayList<String> bubba1 = new ArrayList<>();
        bubba1.add(r1.getStopListA().get(1).getNome());
        bubba1.add(r1.getStopListB().get(4).getNome());
        ArrayList<String> bubba2 = new ArrayList<>();
        bubba2.add(r1.getNameR());
        bubba2.add(r1.getNameR());

        this.childService.registerChild(
                ChildVM.builder()
                        .nameChild("Forrest")
                        .username("user2@info.it")
                        .family_name("Gump")
                        .color("#B0C4DE")
                        .isMale(true)
                        .stopID(bubba)
                        .stopName(bubba1)
                        .nameRoute(bubba2)
                        .direction("entrambi")
                        .build()
        );


    }


    public void insertUserIntoDB(Set<Integer> routeId) {

        Role role = this.roleRepository.findByRole("ROLE_USER");

        this.users.save(User.builder()
                .username("user1@info.it")
                .family_name("Bubba")
                .password(this.passwordEncoder.encode("1user@user"))
                .roles(Arrays.asList(role))
                .muleRoutesID(routeId)
                .isEnabled(true)
                .build()
        );

        this.users.save(User.builder()
                .username("user2@info.it")
                .family_name("Gump")
                .password(this.passwordEncoder.encode("2user@user"))
                .roles(Arrays.asList(role))
                .muleRoutesID(routeId)
                .isEnabled(true)
                .build()
        );


        this.users.save(User.builder()
                .username("user3@info.it")
                .family_name("Dan")
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

}



