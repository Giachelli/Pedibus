package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.viewmodels.RouteVM;
import ai.polito.lab2.demo.viewmodels.StopVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static org.springframework.http.ResponseEntity.ok;



/* TODO fare geolocalizzazione */


@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class RouteController {

    @Autowired
    private RouteService routeService;

    public void PopulateDb() throws IOException {

        routeService.readAll();

    }

    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public ResponseEntity getAllRoutes() throws JsonProcessingException {
        String user = Principal.class.getName();
        List<Route> routes = routeService.getAllRoutes();
        ArrayList<RouteVM> routeVMs = new ArrayList<>();
        System.out.println(user+ " Request GET Lines. The lines are: routes\n");
                routes.forEach(route -> System.out.println(route.getNameR()));
        routes.forEach(route -> {
            ArrayList<StopVM> stopVMsA = new ArrayList<>();
            ArrayList<StopVM> stopVMsB = new ArrayList<>();

            route.getStopListA().forEach(stop -> {
                StopVM stopVM = StopVM.builder()
                        .stopID(stop.get_id().toString())
                        .nameStop(stop.getNome())
                        .time(stop.getTime())
                        .num_s(stop.getNum_s())
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
                        .num_s(stop.getNum_s())
                        .lat(stop.getLat())
                        .lng(stop.getLng())
                        .build();
                stopVMsB.add(stopVM);
            });

            RouteVM r = RouteVM.builder()
                    .id(route.getId())
                    .nameR(route.getNameR())
                    .stopListA(stopVMsA)
                    .stopListB(stopVMsB)
                    .usernameAdmin(route.getUsernameAdmin())
                    .usernameMule(route.getUsernameMule())
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

}
