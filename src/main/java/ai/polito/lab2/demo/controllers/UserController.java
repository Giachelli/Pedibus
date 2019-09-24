package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Route;
import ai.polito.lab2.demo.User;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
public class UserController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepo userRepo;

    @Autowired
    RouteRepo routeRepo;

    @Autowired
    RoleRepo roleRepository;

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    private ArrayList<User> findAllUserinDB()
    {
        return userRepo.findAll();
    }

    @RequestMapping(value = "/users/{userID}", method = RequestMethod.PUT,consumes = MediaType.APPLICATION_JSON_VALUE)
    private void addAdmin(@RequestBody Route nomeLinea, @PathVariable final ObjectId userID, HttpServletRequest req)
    {
        System.out.println(nomeLinea.getNameR());
        User newAdmin = userRepo.findUserBy_id(userID);
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
        String username = jwtTokenProvider.getUsername(token);
        User u = userRepo.findByUsername(username);

        Route r = routeRepo.findRouteByNameR(nomeLinea.getNameR());
        if (r == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route not found");

        if(newAdmin == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");

        if (r.getUsernamesAdmin() != null)
            if(r.getUsernamesAdmin().contains(newAdmin.getUsername()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already admin of the selected route");

        if(u.getRolesString().contains("ROLE_SYSTEM_ADMIN"))
        {

            newAdmin.addRole(roleRepository.findByRole("ROLE_ADMIN"));
            r.addAdmin(newAdmin.getUsername());
        }
        else {

            if (r.getUsernamesAdmin() != null) {
                if (r.getUsernamesAdmin().contains(u.getUsername())) {
                    newAdmin.addRole(roleRepository.findByRole("ROLE_ADMIN"));
                    r.addAdmin(newAdmin.getUsername());
                }
            }
            else
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");

        }

        userRepo.save(newAdmin);
        routeRepo.save(r);

        throw new ResponseStatusException(HttpStatus.OK, "OK");
    }
}
