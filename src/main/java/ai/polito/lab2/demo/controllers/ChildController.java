package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.ReservationRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.ReservationService;
import ai.polito.lab2.demo.viewmodels.ChildVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200")

public class ChildController {

    @Autowired
    private ChildRepo childRepo;
    @Autowired
    private ReservationService reservationService;

    @RequestMapping(value = "/register/child", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChildVM> registerChild(@RequestBody ChildVM data) {

        if (data.getNameChild()== "" || data.getUsername() == null || data.getFamily_name()==null || data.getColor()==null)
            return badRequest().build();

        Child child = Child.builder()
                .nameChild(data.getNameChild())
                .username(data.getUsername())
                .family_name(data.getFamily_name())
                .isMale(data.isMale())
                .color(data.getColor())
                .build();

        System.out.println(data.getUsername());

        childRepo.save(child);

        ChildVM data_return = data;
        data_return.setChildID(child.getChildID().toString());
        return ok().body(data_return);
    }

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
    // vanno aggiunti più query params
    @RequestMapping(value = "/user/children", method = RequestMethod.GET)
    public ResponseEntity getMyChildren(@RequestParam(required = false) String username) {
        System.out.println("entro qui "+username);
        ArrayList<Child> children ;

        ArrayList<ChildVM> childrenVM = new ArrayList<>();


        if(! username.isEmpty() ){
            System.out.println("SONO QUI ");
            children = childRepo.findChildByUsername(username);
            System.out.println("SONO QUI e "+children.size());
        } else {
            children = (ArrayList) childRepo.findAll();
        }

        System.out.println("arrivo qui "+ children.size());
        for (Child r : children) {
            System.out.println("name: " + r.getNameChild());
            childrenVM.add(
                    ChildVM.builder()
                    .childID(r.getChildID().toString())
                    .nameChild(r.getNameChild())
                    .family_name(r.getFamily_name())
                    .color(r.getColor())
                    .username(r.getUsername())
                    .isMale(r.isMale())
                    .build()
            );
        }

        Map<Object, Object> model = new HashMap<>();
        model.put("childrenVM", childrenVM);
        return ok().body(childrenVM);
    }

    @RequestMapping(value = "/user/child", method = RequestMethod.DELETE)
    public void deleteChild(@RequestParam(required = true) ObjectId childID ) {

        ArrayList<ObjectId> reservations_id = new ArrayList<>();
        for (Reservation r:reservationService.findReservationByChildID(childID)){

            reservations_id.add(r.getId());
        }

        reservations_id.forEach( (x) -> {
            reservationService.delete(x);
        });
        childRepo.deleteById(childID);
    }
}

