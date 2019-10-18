package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Service.RouteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public ResponseEntity getAllRoutes() throws JsonProcessingException {
        String user = Principal.class.getName();
        List<Route> routes = routeService.getAllRoutes();
        System.out.println(user+ " Request GET Lines. The lines are:\n");
        routes.forEach(route -> {
            System.out.println(route.getNameR());
        });
        Map<Object, Object> model = new HashMap<>();
        model.put("lines", routes);
        return ok().body(model);
    }

    @RequestMapping(value = "/routes/{nome_linea}", method = RequestMethod.GET)
    public Route getAllStopsForRoute(@PathVariable String nome_linea) throws JsonProcessingException {
        Route route = routeService.getRoutesByName(nome_linea);
        return route;
    }

}
