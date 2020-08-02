package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Role;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.Service.UserService;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.UserRouteVM;
import ai.polito.lab2.demo.viewmodels.UserVM;
import ai.polito.lab2.demo.viewmodels.modifyRoleUserVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class UserController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private RouteService routeService;

    @Autowired
    RoleRepo roleRepository;

    /*@RequestMapping(value = "/users", method = RequestMethod.GET)
    private ArrayList<UserVM> findAllUserinDB() {
        ArrayList<UserDTO> userDTOArrayList = userService.findAll();
        ArrayList<UserVM> userVMs = new ArrayList<>();
        for (UserDTO user : userDTOArrayList) {
            UserVM u = UserVM.builder().username(user.getEmail()).family_name(user.getFamily_name()).build();
            userVMs.add(u);
        }
        return userVMs;

    }*/

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<List<UserVM>> findAllUserinDB() {
        List<User> users = userRepo.findAll();
        ArrayList<String> usersIDString = new ArrayList<>();
        ArrayList<UserVM> userVMS = new ArrayList<>();

        for (User u : users) {
            UserVM userVM = UserVM.builder()
                    .userID(u.get_id().toString())
                    .username(u.getUsername())
                    .family_name(u.getFamily_name())
                    .build();
            usersIDString.add(u.get_id().toString());
            System.out.println(u.get_id().toString());
            userVMS.add(userVM);
        }


        return new ResponseEntity<List<UserVM>>(userVMS, HttpStatus.OK);

    }

    @RequestMapping(value = "/users/{userID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addAdmin(@RequestBody int idLinea, @PathVariable final ObjectId userID, HttpServletRequest req) {
        RouteDTO r = routeService.getRoutesDTOByID(idLinea);
        if (r == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route not found");


        System.out.println(r.getNameR());
        //User newAdmin = userService.getUserBy_id(userID);
        User newAdmin = userService.getUserBy_id(userID);
        if (newAdmin == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");

        String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
        String username = jwtTokenProvider.getUsername(token);
        UserDTO u = userService.getUserDTOByUsername(username);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(idLinea);

        if (r.getUsernamesAdmin() != null)
            if (r.getUsernamesAdmin().contains(newAdmin.getUsername()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already admin of the selected route");


        if (u.getRolesString().contains("ROLE_SYSTEM_ADMIN")) {
            newAdmin.addRole(roleRepository.findByRole("ROLE_ADMIN"));
            newAdmin.addAdminRoutesID(ids);
            r.addAdmin(newAdmin.getUsername());
        } else {
            if (r.getUsernamesAdmin() != null) {
                if (r.getUsernamesAdmin().contains(u.getEmail())) {
                    newAdmin.addRole(roleRepository.findByRole("ROLE_ADMIN"));
                    newAdmin.addAdminRoutesID(ids);
                    r.addAdmin(newAdmin.getUsername());
                }
            } else
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");

        }

        Route route = routeService.getRoutesByID(idLinea);
        route.setUsernameAdmin(r.getUsernamesAdmin());
        userService.saveUser(newAdmin);
        routeService.saveRoute(route);


        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/disenabled", method = RequestMethod.PUT)
    public ResponseEntity disenabledUser(@PathVariable ObjectId userID) {
        User u = userService.getUserBy_id(userID);
        u.setEnabled(false);
        userService.saveUser(u);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/enabled", method = RequestMethod.PUT)
    public ResponseEntity enabledUser(@PathVariable ObjectId userID) {
        User u = userService.getUserBy_id(userID);
        u.setEnabled(true);
        userService.saveUser(u);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteUserByID(@PathVariable ObjectId userID) {
        userService.deleteUserbyID(userID);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /*@Secured("ROLE_SYSTEM_ADMIN") //per Cancellare un utente utilizzando il suo username
    @RequestMapping(value = "/users/{username}/delete", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        userService.deleteUserbyID(user.get_id());
    }*/


    //TODO
    @Secured({"ROLE_ADMIN", "ROLE_SYSTEM_ADMIN"})
    @RequestMapping(value = "/users/modify/{userID}", method = RequestMethod.PUT)
    public ResponseEntity modifyUserByID(@PathVariable ObjectId userID, @RequestBody modifyRoleUserVM modifyRoleUser) {
        User user = userService.getUserBy_id(userID);
        ArrayList<Integer> adminRoutes = modifyRoleUser.getNewAdminRoutes();
        ArrayList<Integer> muleRoutes = modifyRoleUser.getNewMuleRoutes();

        ArrayList<Integer> adminRouteID = new ArrayList<>();
        ArrayList<Integer> muleRouteID = new ArrayList<>();


        //Check if the array of integers passed with the request is empty (admin routes id case)
        if (adminRoutes.size() == 0) {
            //if the array is empty = the user isn't admin for any routes and we delete the role "admin" from his role list
            if (user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN")))
                user.removeRole(roleRepository.findByRole("ROLE_ADMIN"));
        }
        else {
            //add the id routes in the user list
            for (int i : adminRoutes) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    System.out.println("Errore nella modify USer passo un id non esistente");
                }
                adminRouteID.add(i);
                Route addAdminRoute = routeService.getRoutesByName(r.getNameR());
                addAdminRoute.addAdmin(user.getUsername());
                routeService.saveRoute(addAdminRoute);
            }
            if (adminRoutes.size() > 0) {
                if (!user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN")))
                    user.addRole(roleRepository.findByRole("ROLE_ADMIN"));
            }

            user.addAdminRoutesID(adminRouteID);
        }


        userService.saveUser(user);

        //Check if the array of integers passed with the request is empty (mule routes id case)
        if (muleRoutes.size() == 0) {
            //the same of admin cases
            if (user.getRoles().contains(roleRepository.findByRole("ROLE_MULE")))
                user.removeRole(roleRepository.findByRole("ROLE_MULE"));
        }
        else {
            for (int j : muleRoutes) {
                Route r = routeService.getRoutesByID(j);
                if (r == null) {
                    System.out.println("Errore nella modify USer passo un id non esistente");
                }
                muleRouteID.add(j);
                Route addMuleRoute = routeService.getRoutesByName(r.getNameR());
                addMuleRoute.addMule(user.getUsername());
                routeService.saveRoute(addMuleRoute);
            }

            if (muleRoutes.size() > 0)
                if (!user.getRoles().contains(roleRepository.findByRole("ROLE_MULE"))) {
                    user.addRole(roleRepository.findByRole("ROLE_MULE"));
                }

            user.addMuleRoutesID(muleRouteID);
        }

        userService.saveUser(user);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
/*
    @Secured({"ROLE_ADMIN", "ROLE_SYSTEM_ADMIN"}) //aggiungere ruoli all'utente tramite username
    @RequestMapping(value = "/users/modify/{username}", method = RequestMethod.PUT)
    public ResponseEntity modifyUser(@PathVariable String username, @RequestBody modifyRoleUserVM modifyRoleUser) {
        User user = userService.getUserByUsername(username);
        ArrayList<Integer> adminRoutes = modifyRoleUser.getNewAdminRoutes();
        ArrayList<Integer> muleRoutes = modifyRoleUser.getNewMuleRoutes();

        ArrayList<Integer> adminRouteID = new ArrayList<>();
        ArrayList<Integer> muleRouteID = new ArrayList<>();

        for (Route r : adminRoutes) {
            adminRouteID.add(r.getId());
            Route addAdminRoute = routeService.getRoutesByName(r.getNameR());
            addAdminRoute.addAdmin(user.getUsername());
            routeService.saveRoute(addAdminRoute);
        }
        if (adminRoutes.size() > 0)
            if (!user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN")))
                user.addRole(roleRepository.findByRole("ROLE_ADMIN"));

        user.addAdminRoutesID(adminRouteID);

        //todo questo secondo me è superfluo e inutile 21/03
        userService.saveUser(user);

        //controlli per lista vuota


        for (Route r : muleRoutes) {
            muleRouteID.add(r.getId());
            Route addMuleRoute = routeService.getRoutesByName(r.getNameR());
            addMuleRoute.addMule(user.getUsername());
            routeService.saveRoute(addMuleRoute);
        }

        if (muleRoutes.size() > 0)
            if (!user.getRoles().contains(roleRepository.findByRole("ROLE_MULE"))) {
                user.addRole(roleRepository.findByRole("ROLE_MULE"));
            }

        user.addMuleRoutesID(muleRouteID);

        userService.saveUser(user);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }*/


    // TODO dobbiamo vedere se modificando qualcosa si riblocca il resto da altre parti
    @RequestMapping(value = "/users/{userID}/getAdminLines", method = RequestMethod.GET)
    public ResponseEntity<UserRouteVM> getUserLines(@RequestBody ObjectId userID) {
        User user = userService.getUserBy_id(userID);
        ArrayList<Integer> adminRoute = new ArrayList<>();
        ArrayList<Integer> muleRoute = new ArrayList<>();

        for (int i : user.getAdminRoutesID()) {
            Route r = routeService.getRoutesByID(i);
            if (r == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error here!! Route non esistente");
            }
            adminRoute.add(i);
        }
        for (int i : user.getMuleRoutesID()) {
            Route r = routeService.getRoutesByID(i);
            if (r == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error here!! Route non esistente");
            }
            muleRoute.add(i);
        }

        UserRouteVM userVM = UserRouteVM.builder()
                .userID(user.get_id())
                .adminRoute(adminRoute)
                .muleRoute(muleRoute)
                .build();

        return new ResponseEntity<UserRouteVM>(userVM, HttpStatus.OK);
    }


}
