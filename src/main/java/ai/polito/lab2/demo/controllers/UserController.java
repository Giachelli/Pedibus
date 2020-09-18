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
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private ChildService childService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ShiftService shiftService;

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
                    .availabilityVM((u.getAvailability()))
                    .isEnabled(u.isEnabled())
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

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/users/{userID}/disabled", method = RequestMethod.PUT)
    public ResponseEntity disabledUser(@PathVariable ObjectId userID) {
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

        if((modifyRoleUser.getAvailability() == null)||(modifyRoleUser.getStopAndata() == null) || (modifyRoleUser.getStopRitorno() == null))
            return new ResponseEntity("Errore nel passaggio dei parametri",HttpStatus.BAD_REQUEST);

        //if(controlData())

        User user = userService.getUserBy_id(userID);
        ArrayList<Integer> adminRoutes = modifyRoleUser.getAdminRoutes();
        ArrayList<Integer> muleRoutes = modifyRoleUser.getMuleRoutes();

        ArrayList<Boolean> oldAvailability = user.getAvailability();

        ArrayList<Integer> adminBefore = new ArrayList<>();
        ArrayList<Integer> muleBefore = new ArrayList<>();


        Set<Integer> adminRouteID = new HashSet<>();
        Set<Integer> muleRouteID = new HashSet<>();

        if (user.getAdminRoutesID() != null) // lo user è admin di qualche linea
            for (int i : user.getAdminRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    System.out.println("Errore nella modify User passo un id route non esistente");
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                if(r.getUsernameAdmin().contains(user.getUsername())) {
                    r.removeAdmin(user.getUsername());
                    adminBefore.add(i);
                }
                else
                {
                    throw  new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Settaggio db errore");
                }
                routeService.saveRoute(r);
            }

        if (user.getMuleRoutesID() != null)
            for (int i : user.getMuleRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    System.out.println("Errore nella modify USer passo un id route non esistente");
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);

                }

                if(r.getUsernameMule().contains(user.getUsername())) {
                    r.removeMule(user.getUsername());
                    muleBefore.add(i);
                }else
                {
                    throw  new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Settaggio db errore");
                }

                routeService.saveRoute(r);
            }


        //Check if the array of integers passed with the request is empty (admin routes id case)
        if (adminRoutes.size() == 0) {
            //if the array is empty = the user isn't admin for any routes and we delete the role "admin" from his role list
            if (user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN"))){
                user.removeRole(roleRepository.findByRole("ROLE_ADMIN"));
            }

        } else {
            //add the id routes in the user list
            //todo vedere se ci sono idee migliori
            for (int i : adminRoutes) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    System.out.println("Errore nella modify USer passo un id non esistente");
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                // controllo che non sia già Admin per la linea
                adminRouteID.add(i);
                Route addAdminRoute = routeService.getRoutesByName(r.getNameR());
                if(addAdminRoute.getUsernameAdmin() == null)
                {
                    addAdminRoute.addAdmin(user.getUsername());
                    routeService.saveRoute(addAdminRoute);
                }
                if (!addAdminRoute.getUsernameAdmin().contains(user.getUsername())) {
                    addAdminRoute.addAdmin(user.getUsername());
                    routeService.saveRoute(addAdminRoute);
                }
            }
            if (adminRoutes.size() > 0)
                if (!user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN")))
                    user.addRole(roleRepository.findByRole("ROLE_ADMIN"));
        }

        user.setAdminRoutesID(adminRouteID);
        userService.saveUser(user);

        //Check if the array of integers passed with the request is empty (mule routes id case)
        if (muleRoutes.size() == 0) {
            //the same of admin cases
            if (user.getRoles().contains(roleRepository.findByRole("ROLE_MULE"))){
                user.removeRole(roleRepository.findByRole("ROLE_MULE"));
            }
        } else {
            for (int j : muleRoutes) {
                Route r = routeService.getRoutesByID(j);
                if (r == null) {
                    System.out.println("Errore nella modify USer passo un id non esistente");
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                // controllo che non sia già Mule per la linea

                muleRouteID.add(j);
                Route addMuleRoute = routeService.getRoutesByName(r.getNameR());
                if (addMuleRoute.getUsernameMule() == null)
                {
                    addMuleRoute.addMule(user.getUsername());
                    routeService.saveRoute(addMuleRoute);
                }
                if (!addMuleRoute.getUsernameMule().contains(user.getUsername()) ) {
                    addMuleRoute.addMule(user.getUsername());
                    routeService.saveRoute(addMuleRoute);
                }
            }

            if (muleRoutes.size() > 0)
                if (!user.getRoles().contains(roleRepository.findByRole("ROLE_MULE"))) {
                    user.addRole(roleRepository.findByRole("ROLE_MULE"));
                }

        }
        user.setMuleRoutesID(muleRouteID);
        user.setAvailability(modifyRoleUser.getAvailability());
        user.setAndataStops(modifyRoleUser.getStopAndata());
        user.setRitornoStops(modifyRoleUser.getStopRitorno());
        userService.saveUser(user);

            long day = new Date().getTime();
            HashMap<Integer,ArrayList<String>> otherAdmins = new HashMap<Integer, ArrayList<String>>(); // mappa che contiene le linee a cui non sarò più admin e i relativi admin
            ArrayList<Integer> old_newRoute = new ArrayList<>();
            old_newRoute.addAll(adminBefore);
            old_newRoute.addAll(adminRoutes);
            old_newRoute.addAll(muleBefore);
            old_newRoute.addAll(muleRoutes);

            /* messaggio che deve arrivare agli admin di linea */

            for (Integer i : old_newRoute){
                if(otherAdmins.containsKey(i)) //evito di mandare due notifiche agli admin delle linee che rimangono invariate
                    continue;
                Route r = routeService.getRoutesByID(i);
                if (r.getUsernameAdmin()!=null && r.getUsernameAdmin().size()!=0){
                    for ( String s : r.getUsernameAdmin()){
                        if (s.equals(user.getUsername()))
                            continue;
                        if (otherAdmins.containsKey(i)) //entra dalla seconda volta in poi
                            otherAdmins.get(i).add(s);
                        else{ // entra la prima volta
                            otherAdmins.put(i,new ArrayList<String>());
                            otherAdmins.get(i).add(s);
                        }
                    }
                }
            }
            for (Map.Entry<Integer,ArrayList<String>> entry: otherAdmins.entrySet()){
                String action = "I privilegi/disponibilità relativi allo user " + user.getUsername() + " aggiornati.";
                // mettere controllo che se entry.getValue è uguale al sender, allora il messaggio non va inviato
                if (entry.getValue().contains(modifyRoleUser.getModifiedBy())){
                    entry.getValue().remove(modifyRoleUser.getModifiedBy());  // in questo modo il messaggio non dovrebbe arrivare a chi ha fatto l'operazione anche se admin di un altra linea per cui lo user ha subito delle variazioni
                }
                messageService.createMessageNewRolesOtherAdmins(modifyRoleUser.getModifiedBy(),
                        entry.getValue(),
                        action,
                        day,
                        entry.getKey());

                /* se anche gli admin posso cambiare le availability di un mule allora ritornare su questo messaggio, poiché lo user non viene informato*/

                /* Da utilizzare solo nel caso vogliamo notificare che uno user ha modificato le sue dispo, sennò no.
                if (!(oldAvailability.equals(modifyRoleUser.getAvailability()))){
                    String action1= "Modifica disponibilità";
                    messageService.createMessageEditAvailability(modifyRoleUser.getModifiedBy(),
                            entry.getValue(),
                            action1,
                            day,
                            entry.getKey());
                }
                */


            }

        /* seconda parte che riguarda lo user stesso */
        if (!(modifyRoleUser.getModifiedBy().equals(userService.getUserBy_id(user.get_id()).getUsername()))){
            String action= "Privilegi aggiornati";
            messageService.createMessageNewRoles(modifyRoleUser.getModifiedBy(),
                    user.get_id(),
                    action,
                    day,
                    adminRoutes,
                    muleRoutes);
        }




        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/users/{userID}/getLines", method = RequestMethod.GET)
    public ResponseEntity<UserRouteVM> getUserLines(@PathVariable ObjectId userID) {
        User user = userService.getUserBy_id(userID);
        ArrayList<Integer> adminRoute = new ArrayList<>();
        ArrayList<Integer> muleRoute = new ArrayList<>();
        if (user.getAdminRoutesID() != null)
            for (int i : user.getAdminRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error here!! Route non esistente");
                }
                adminRoute.add(r.getId());
            }
        if (user.getMuleRoutesID() != null)
            for (int i : user.getMuleRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error here!! Route non esistente");
                }
                muleRoute.add(r.getId());
            }

        UserRouteVM userVM = UserRouteVM.builder()
                .userID(user.get_id())
                .username(user.getUsername())
                .adminRoutes(adminRoute)
                .muleRoutes(muleRoute)
                .availability(user.getAvailability())
                .build();

        return new ResponseEntity<UserRouteVM>(userVM, HttpStatus.OK);
    }

/**
*
*
* @return totale informazioni di sistema
*
*/
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


}
