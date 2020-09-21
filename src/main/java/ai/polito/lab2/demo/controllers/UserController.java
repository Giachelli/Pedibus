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
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.*;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

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
        List<UserVM> usersVMS = userService.getAllUser();
        return new ResponseEntity<List<UserVM>>(usersVMS, HttpStatus.OK);

    }

    /*
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

        String token = jwtTokenProvider.resolveToken(req);
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
*/
    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/disabled", method = RequestMethod.PUT)
    public ResponseEntity disabledUser(@PathVariable ObjectId userID) {
        if (userService.getUserBy_id(userID) == null) {
            return new ResponseEntity("Errore User non presente nel db", HttpStatus.BAD_REQUEST);
        }
        userService.disableUser(userID);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/enabled", method = RequestMethod.PUT)
    public ResponseEntity enabledUser(@PathVariable ObjectId userID) {
        if (userService.getUserBy_id(userID) == null) {
            return new ResponseEntity("Errore User non presente nel db", HttpStatus.BAD_REQUEST);
        }
        userService.ableUser(userID);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteUserByID(@PathVariable ObjectId userID) {
        if (userService.getUserBy_id(userID) == null) {
            return new ResponseEntity("Errore User non presente nel db", HttpStatus.BAD_REQUEST);
        }
        userService.deleteUserbyID(userID);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured({"ROLE_ADMIN", "ROLE_SYSTEM_ADMIN", "ROLE_MULE"})
    @RequestMapping(value = "/users/modify/{userID}", method = RequestMethod.PUT)
    public ResponseEntity modifyUserByID(@PathVariable ObjectId userID, @RequestBody modifyRoleUserVM modifyRoleUser) {

        if (userService.getUserBy_id(userID) == null) {
            return new ResponseEntity("Errore User non presente nel db", HttpStatus.BAD_REQUEST);
        }

        if ((modifyRoleUser.getAvailability() == null) || (modifyRoleUser.getStopAndata() == null) || (modifyRoleUser.getStopRitorno() == null) )
            return new ResponseEntity("Errore nel passaggio dei parametri", HttpStatus.BAD_REQUEST);

        try {
            userService.editUser(userID, modifyRoleUser);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @Secured({"ROLE_ADMIN","ROLE_SYSTEM_ADMIN","ROLE_MULE"})
    @RequestMapping(value = "/users/{userID}/getLines", method = RequestMethod.GET)
    public ResponseEntity getUserLines(@PathVariable ObjectId userID) {
        if (userService.getUserBy_id(userID) == null) {
            return new ResponseEntity("Errore User non presente nel db", HttpStatus.BAD_REQUEST);
        }

        UserRouteVM userVM = userService.getRoutesUser(userID);

        return new ResponseEntity(userVM, HttpStatus.OK);
    }


/*
    @Secured("ROLE_SYSTEM_ADMIN")
    @ApiOperation("Restituisce al solo system admin le informazioni generali sdll'app, numero di bimbi iscritti, numero di utenti presenti")
    @RequestMapping(value = "/users/info", method = RequestMethod.GET)
    public ResponseEntity getInfoAboutPedibus() {

        List<RouteVM> routes = routeService.getAllRoutes();
        Set<String> admin = new HashSet<>();
        Set<String> mule = new HashSet<>();
        int numberRoutes = routes.size();

        int numberMule = 0;
        int numberAdmin = 0;

        for (RouteVM r : routes)
        {
            for(int i =0; i< r.getUsernameAdmin().size(); i++)
                admin.add(r.getUsernameAdmin().get(i).getUsername());


            for(int i =0; i< r.getUsernameMule().size(); i++)
                mule.add(r.getUsernameMule().get(i).getUsername());


            }


        int userNumber = userService.findAll().size() -1;
        int childNumber = childService.findAllChild().size();

        numberAdmin = admin.size();
        numberMule = mule.size();

        int reservationToday = reservationService.findNumberReservationToday();
        int muleToday = shiftService.findNumberShiftToday();


        DashboardVM dashboardVM = DashboardVM.builder()
                .numberRoutes(numberRoutes)
                .numberAdmin(numberAdmin).numberMules(numberMule)
                .numberChild(childNumber)
                .numberUser(userNumber)
                .reservationToday(reservationToday)
                .muleActiveToday(muleToday)
                .build();
        return new ResponseEntity<DashboardVM>(dashboardVM,HttpStatus.OK);
    }
*/

}
