package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.viewmodels.ChildAllVM;
import ai.polito.lab2.demo.viewmodels.ChildVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200")

public class ChildController {

    @Autowired
    private ChildRepo childRepo;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private StopService stopService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private ChildService childService;


    /**
     * @param data view model del child
     * @return
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @Secured("ROLE_USER")
    @ApiOperation("Endpoint per la registrazione di un bambino")
    @RequestMapping(value = "/register/child", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Bimbo creato"),
            @ApiResponse(code = 400, message = "Richiesta mal formata"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Richiesta non permessa")
    })
    public ResponseEntity<ChildVM> registerChild(@RequestBody ChildVM data) {

        // non sono required lo stopID, la lineaID, la direction e la data
        //TODO: mettere controllo anche che il nome sia well formed con regex
        if (data.getNameChild() == ""
                || data.getNameChild()==null
                || data.getNameChild().length()<3
                || data.getFamily_name() == ""
                || data.getUsername() == null
                || data.getUsername() == ""
                || data.getFamily_name() == null
                || data.getColor() == null)
            return new ResponseEntity("Dati inseriti in modo non corretto", HttpStatus.BAD_REQUEST);

        /* Controllo per vedere se stiamo inserendo un bambino con lo stesso nome */

        ChildVM childVM = childService.registerChild(data);
        if (childVM==null){
            return new ResponseEntity("Richiesta mal formata", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(childVM,HttpStatus.CREATED);
    };

    /* LASCIARE COMMENTATA E IN FASE DI TEST GENERALE VEDERE SE VIENE USATA
    @RequestMapping(value = "/user/{userID}/children", method = RequestMethod.GET)
    public ResponseEntity getMyChilds(@PathVariable String userID) {



        ArrayList<Child> children = childRepo.findChildByUsername(userID);

        ArrayList<String> childrenName = new ArrayList<>();
        for (Child r : children) {
            childrenName.add(r.getNameChild());

        }

        Map<Object, Object> model = new HashMap<>();
        model.put("children", childrenName);
        return ok(model);
    }
    */


    @Secured("ROLE_USER")
    @ApiOperation("Endpoint per avere i bambini registrati da uno user")
    @RequestMapping(value = "/user/children", method = RequestMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Richiesta mal formata"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Richiesta non permessa")
    })
    public ResponseEntity getMyChildren(@RequestParam(required = true) String username) {
        if (username.isEmpty() && username == null){
            return new ResponseEntity("Richiesta mal formata", HttpStatus.BAD_REQUEST);
        }
        ArrayList<ChildVM> childrenVM = childService.getMyChildren(username);
        if (childrenVM == null)
            return new ResponseEntity("Lo user non esiste", HttpStatus.BAD_REQUEST);

        return new ResponseEntity(childrenVM, HttpStatus.OK);
    }

    @Secured({"ROLE_USER","ROLE_SYSTEM_ADMIN"})
    @ApiOperation("Endpoint per cancellare i bambini registrati da uno user")
    @RequestMapping(value = "/user/child", method = RequestMethod.DELETE)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Bimbo cancellato con successo"),
            @ApiResponse(code = 400, message = "Richiesta mal formata"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Richiesta non permessa"),
            @ApiResponse(code = 404, message = "Bimbo non trovato")
    })
    public ResponseEntity deleteChild(@RequestParam(required = true) ObjectId childID ) {

        if (childID == null)
            return new ResponseEntity("Fornire un childID valido", HttpStatus.BAD_REQUEST);

        childService.deleteChild(childID);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ApiOperation("Endpoint per avere tutti i bambini registrati a sistema")
    @RequestMapping(value = "/children/all", method = RequestMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Richiesta non permessa"),
    })
    public ResponseEntity getChildren() {

        ArrayList<ChildAllVM> childrenVM = childService.findAllChildren();

        return new ResponseEntity(childrenVM, HttpStatus.OK);

    }
}

