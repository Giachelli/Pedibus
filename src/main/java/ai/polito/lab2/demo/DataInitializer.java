package ai.polito.lab2.demo;



import static reactor.bus.selector.Selectors.$;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Role;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.controllers.RouteController;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import org.springframework.context.annotation.Bean;
import reactor.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.bus.EventBus;

import java.util.Arrays;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepo users;

    @Autowired
    private RouteController routeController;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private ChildRepo childRepo;

    @Override
    public void run(String... args) throws Exception {
        routeController.PopulateDb();
        setRoleDb();
        if (users.findByUsername("admin@info.it") != null) {
            //do nothing
        } else {

            this.users.save(User.builder()
                    .username("admin@info.it")
                    .password(this.passwordEncoder.encode("12345@dmin"))
                    .roles(Arrays.asList(roleRepository.findByRole("ROLE_USER"),
                            roleRepository.findByRole("ROLE_SYSTEM_ADMIN"),
                            roleRepository.findByRole("ROLE_MULE")))
                    .isEnabled(true)
                    .build()
            );
            insertUserIntoDB();
            insertChildIntoDB();
        }
/*
        log.debug("printing all users...");
        this.users.findAll().forEach(v -> log.debug(" User :" + v.toString()));*/
    }

    private void insertChildIntoDB() {
        System.out.println("creano il child");
        this.childRepo.save(
                Child.builder()
                        .nameChild("Luigi")
                        .username("user1@info.it")
                        .family_name("aaa")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Mario")
                        .username("user2@info.it")
                        .family_name("bb")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Franco")
                        .username("user2@info.it")
                        .family_name("aaaassa")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Giacomo")
                        .family_name("cccccccc")
                        .username("user3@info.it")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Francesco")
                        .username("user1@info.it")
                        .family_name("fffddddd")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Mauro")
                        .family_name("ddddwwwwaaa")
                        .username("user3@info.it")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("David")
                        .family_name("adffdfdaa")
                        .username("user1@info.it")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Jack")
                        .family_name("aaaaffdfdgf")
                        .username("user3@info.it")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Paolo")
                        .family_name("ssddeeeaaa")
                        .username("user4@info.it")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Antonio")
                        .family_name("fgrtraa")
                        .username("user6@info.it")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Giovanni")
                        .family_name("adfgfgfgaa")
                        .username("user2@info.it")
                        .isMale(true)
                        .build()
        );
        this.childRepo.save(
                Child.builder()
                        .nameChild("Caterina")
                        .family_name("afdeereaa")
                        .username("user2@info.it")
                        .isMale(false)
                        .build()
        );


    }


    public void insertUserIntoDB() {

        Role role = this.roleRepository.findByRole("ROLE_USER");
        this.users.save(User.builder()
                .username("user1@info.it")
                .family_name("Malnati")
                .password(this.passwordEncoder.encode("1user@user"))
                //.roles(Arrays.asList(role, roleRepository.findByRole("ROLE_ADMIN")))
                .roles(Arrays.asList(role))
                .isEnabled(true)
                .build()
        );

        this.users.save(User.builder()
                .username("user2@info.it")
                .family_name("Servetti")
                .password(this.passwordEncoder.encode("2user@user"))
                .roles(Arrays.asList(role))
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


}

