package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Route;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.Service.UserService;
import ai.polito.lab2.demo.viewmodels.RouteVM;
import ai.polito.lab2.demo.viewmodels.StopVM;
import ai.polito.lab2.demo.viewmodels.UserVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static org.springframework.http.ResponseEntity.ok;


@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class RouteController {

    @Autowired
    private RouteService routeService;
    @Autowired
    private UserService userService;

    public void PopulateDb() throws IOException {

        routeService.readAll();

    }


    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public ResponseEntity getAllRoutes() throws JsonProcessingException {
        String user = Principal.class.getName();
        List<Route> routes = routeService.getAllRoutes();
        ArrayList<RouteVM> routeVMs = new ArrayList<>();
        System.out.println(user + " Request GET Lines. The lines are: routes\n");
        routes.forEach(route -> System.out.println(route.getNameR()));
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

        Map<Object, Object> model = new HashMap<>();
        model.put("lines", routeVMs);
        return ok().body(model);
    }

    @RequestMapping(value = "/routes/{nome_linea}", method = RequestMethod.GET)
    public ResponseEntity<Route> getAllStopsForRoute(@PathVariable String nome_linea) throws JsonProcessingException {
        Route route = routeService.getRoutesByName(nome_linea);
        return new ResponseEntity<Route>(route, HttpStatus.OK);
    }

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/routes/addRoute", method = RequestMethod.POST)
    public ResponseEntity createRoute(@RequestPart("file") MultipartFile file) throws JsonProcessingException {
        if (null == file.getOriginalFilename()) {
            return new ResponseEntity<>("File senza titolo",HttpStatus.BAD_REQUEST);
        }
        RouteVM routeVM;
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(file.getOriginalFilename());
            Files.write(path, bytes);
            System.out.println(path.getFileName());
            Route r = routeService.readSingle(path.toFile());
            /*File myObj = new File(path.getFileName().toString());
            if (myObj.delete()) {
                System.out.println("Deleted the file: " + myObj.getName());
            } else {
                System.out.println("Failed to delete the file.");
            }*/
            Path result = null;
            ArrayList<StopVM> stopVMsA = new ArrayList<>();
            ArrayList<StopVM> stopVMsB = new ArrayList<>();
            List<UserVM> muleVMList = new ArrayList<>();
            List<UserVM> adminVMList = new ArrayList<>();

            r.getStopListA().forEach(stop -> {
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

            r.getStopListB().forEach(stop -> {
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

            if (r.getUsernameAdmin() != null)
                for (String u : r.getUsernameAdmin()) {
                    User admin = userService.getUserByUsername(u);
                    UserVM adminVM = UserVM.builder()
                            .userID(admin.get_id().toString())
                            .username(u)
                            .family_name(admin.getFamily_name())
                            .isEnabled(admin.isEnabled())
                            .build();
                    adminVMList.add(adminVM);
                }

            if (r.getUsernameMule() != null)
                for (String u : r.getUsernameMule()) {
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

            routeVM = RouteVM.builder()
                    .id(r.getId())
                    .nameR(r.getNameR())
                    .stopListA(stopVMsA)
                    .stopListB(stopVMsB)
                    .usernameAdmin(adminVMList)
                    .usernameMule(muleVMList)
                    .build();
            try {
                result = Files.move(Paths.get(path.getFileName().toString()), Paths.get("./target/pedibus_routes/"+path.getFileName().toString()));
            } catch (IOException e) {
                System.out.println("Exception while moving file: " + e.getMessage());
            }
            if(result != null) {
                System.out.println("File moved successfully.");
            }else{
                System.out.println("File movement failed.");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Errore nel file passato",HttpStatus.BAD_REQUEST);
        }

        //TODO mettere mail qui

        return new ResponseEntity<>(routeVM, HttpStatus.CREATED);

    }

}
