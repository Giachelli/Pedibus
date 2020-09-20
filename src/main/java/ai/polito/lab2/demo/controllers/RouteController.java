package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Entity.Route;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.OnNewFileCompleteEvent;
import ai.polito.lab2.demo.viewmodels.AllRoutesVM;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.context.ApplicationEventPublisher;
import ai.polito.lab2.demo.Service.RouteService;
import ai.polito.lab2.demo.Service.UserService;
import ai.polito.lab2.demo.viewmodels.RouteVM;
import ai.polito.lab2.demo.viewmodels.StopVM;
import ai.polito.lab2.demo.viewmodels.UserVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    ApplicationEventPublisher eventPublisher;

    private final static Logger logger = LoggerFactory.getLogger(RouteController.class);

    /**
     * funzione chiamata in fase di inizializzazione che legge da file e salva tutte le linee su mongo
     * @throws IOException
     */
    public void PopulateDb() throws IOException {

        routeService.readAll();

    }

    /**
     * ritorna tutte le linee presenti sul db
     * @return ritorna varie informazioni sulle linee presenti nel db
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    @ApiOperation("ritorna tutte le linee presenti sul db")
    public ResponseEntity getAllRoutes() throws JsonProcessingException {
        String user = Principal.class.getName();
        List<RouteVM> routeVMs = routeService.getAllRoutes();

        logger.info(user + " Request GET Lines. The lines are: routes\n");
        Map<Object, Object> model = new HashMap<>();
        AllRoutesVM allRoutesVM = AllRoutesVM.builder().lines(routeVMs).build();
        return new ResponseEntity(allRoutesVM,HttpStatus.OK);
    }

    /**
     * ritorna una linea dal nome
     * @param id_linea id della linea richiesta
     * @return linea per intero
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/routes/{id_linea}", method = RequestMethod.GET)
    @ApiOperation("ritorna una linea in base all'id")
    public ResponseEntity<RouteVM> getAllStopsForRoute(@ApiParam("Id della linea") @PathVariable int id_linea) throws JsonProcessingException {
        RouteVM route = routeService.getRoutesVMByID(id_linea);
        return new ResponseEntity<>(route, HttpStatus.OK);
    }

    /**
     * aggiunta di una linea tramite file json
     * @param file file contenente la linea da aggiungere
     * @param request
     * @return
     * @throws JsonProcessingException
     */
    @Secured("ROLE_SYSTEM_ADMIN")
    @ApiOperation("aggiunta di una linea tramite file json")
    @RequestMapping(value = "/routes/addRoute", method = RequestMethod.POST)
    public ResponseEntity createRoute(@RequestPart("file") MultipartFile file, WebRequest request) throws JsonProcessingException {
        if (null == file.getOriginalFilename()) {
            return new ResponseEntity<>("File senza titolo",HttpStatus.BAD_REQUEST);
        }
        List<UserVM> adminVMList = new ArrayList<>();
        RouteVM routeVM;
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(file.getOriginalFilename());
            Files.write(path, bytes);
            routeVM = routeService.readSingle(path.toFile());
            /*File myObj = new File(path.getFileName().toString());
            if (myObj.delete()) {
                System.out.println("Deleted the file: " + myObj.getName());
            } else {
                System.out.println("Failed to delete the file.");
            }*/
            Path result = null;

            try {
                //System.out.println(ResourceUtils.getFile("classpath:pedibus_routes/"));
                result = Files.move(Paths.get(path.getFileName().toString()), Paths.get(ResourceUtils.getFile("classpath:pedibus_routes//")+"/"+path.getFileName().toString()));
            } catch (IOException e) {
                logger.error("Exception while moving file: " + e.getMessage());
            }
            if(result != null) {
                logger.info("File moved successfully.");
            }else{
                logger.error("File movement failed.");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Errore nel file passato",HttpStatus.BAD_REQUEST);
        }

        try {
            if (adminVMList!= null){
                for ( UserVM user : adminVMList){
                    String appUrl = request.getContextPath();
                    eventPublisher.publishEvent(new OnNewFileCompleteEvent
                            (user, request.getLocale(), appUrl));
                }
            }
        } catch (Exception me) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Some problems occurred when sending the email", me);
        }

        return new ResponseEntity<>(routeVM, HttpStatus.CREATED);

    }

}
