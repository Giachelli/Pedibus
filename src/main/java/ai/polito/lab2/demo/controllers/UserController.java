package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Entity.Role;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.RouteRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.Service.UserService;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.UserVM;
import ai.polito.lab2.demo.viewmodels.modifyRoleUserVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@RestController
public class UserController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private RouteService routeService;

    @Autowired
    RoleRepo roleRepository;

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    private ArrayList<UserVM> findAllUserinDB() {
        ArrayList<UserDTO> userDTOArrayList = userService.findAll();
        ArrayList<UserVM> userVMs = new ArrayList<>();
        for (UserDTO user : userDTOArrayList) {
            UserVM u = UserVM.builder().username(user.getEmail()).family_name(user.getFamily_name()).build();
            userVMs.add(u);
        }
        return userVMs;

    }

    @RequestMapping(value = "/users/{userID}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    private void addAdmin(@RequestBody Route nomeLinea, @PathVariable final ObjectId userID, HttpServletRequest req) {
        System.out.println(nomeLinea.getNameR());
        //User newAdmin = userService.getUserBy_id(userID);
        UserDTO newAdmin = userService.getUserDTOBy_id(userID);
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
        String username = jwtTokenProvider.getUsername(token);
        UserDTO u = userService.getUserByUsername(username);

        RouteDTO r = routeService.findRouteByNameR(nomeLinea.getNameR());
        if (r == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route not found");

        if (newAdmin == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");

        if (r.getUsernamesAdmin() != null)
            if (r.getUsernamesAdmin().contains(newAdmin.getEmail()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already admin of the selected route");

        if (u.getRolesString().contains("ROLE_SYSTEM_ADMIN")) {

            newAdmin.addRole(roleRepository.findByRole("ROLE_ADMIN"));
            r.addAdmin(newAdmin.getEmail());
        } else {

            if (r.getUsernamesAdmin() != null) {
                if (r.getUsernamesAdmin().contains(u.getEmail())) {
                    newAdmin.addRole(roleRepository.findByRole("ROLE_ADMIN"));
                    r.addAdmin(newAdmin.getEmail());
                }
            } else
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");

        }


        userService.saveUser(newAdmin);
        routeService.saveRoute(r);


        throw new ResponseStatusException(HttpStatus.OK, "OK");
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/disenabled", method = RequestMethod.PUT)
    public void disenabledUser(@PathVariable ObjectId userID) {
        User u = userService.getUserBy_id(userID);
        u.setEnabled(false);
        userService.saveUser(u);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/enabled", method = RequestMethod.PUT)
    public void enabledUser(@PathVariable ObjectId userID) {
        User u = userService.getUserBy_id(userID);
        u.setEnabled(true);
        userService.saveUser(u);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/delete", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable ObjectId userID) {
        userService.deleteUserbyID(userID);
    }

    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/users/modify/{userID}", method = RequestMethod.PUT)
    public void modifyUser(@PathVariable ObjectId userID, @RequestBody modifyRoleUserVM modifyRoleUser) {
        User user = userService.getUserBy_id(userID);
        ArrayList<Route> adminRoutes = modifyRoleUser.getNewAdminRoutes();
        ArrayList<Route> muleRoutes = modifyRoleUser.getNewMuleRoutes();

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

        userService.saveUser(user);

        //controlli per lista vuota
        //TODO lista di accompagantori nella ROUTE

        for (Route r : muleRoutes) {
            muleRouteID.add(r.getId());
            Route addMuleRoute = routeService.getRoutesByName(r.getNameR());
            addMuleRoute.addAdmin(user.getUsername());
            routeService.saveRoute(addMuleRoute);
        }

        if (muleRoutes.size() > 0)
            if (!user.getRoles().contains(roleRepository.findByRole("ROLE_MULE"))) {
                user.addRole(roleRepository.findByRole("ROLE_MULE"));
            }

        user.addMuleRoutesID(muleRouteID);

        userService.saveUser(user);
    }


}
