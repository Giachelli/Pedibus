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
    public ResponseEntity<UserRouteVM> getUserLines(@PathVariable ObjectId userID) {
        if (userService.getUserBy_id(userID) == null) {
            return new ResponseEntity("Errore User non presente nel db", HttpStatus.BAD_REQUEST);
        }

        UserRouteVM userVM = userService.getRoutesUser(userID);

        return new ResponseEntity(userVM, HttpStatus.OK);
    }


}
