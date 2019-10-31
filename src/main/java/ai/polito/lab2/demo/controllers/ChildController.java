package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
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

import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200")

public class ChildController {

    @Autowired
    private ChildRepo childRepo;


    @RequestMapping(value = "/register/child", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Child registerChild(@RequestBody ChildVM data) {


        Child child = Child.builder()
                .nameChild(data.getNameChild())
                .username(data.getUsername())
                .build();

        System.out.println(data.getUsername());

        childRepo.save(child);
        return child;
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

        List<Child> children = new ArrayList<>();

        ArrayList<ChildVM> childrenVM = new ArrayList<>();


        if(!(username==null)){
            children = childRepo.findChildByUsername(username);
        } else {
            children = childRepo.findAll();
        }

        for (Child r : children) {
            childrenVM.add(
                    ChildVM.builder()
                    .childID(r.getChildID().toString())
                    .nameChild(r.getNameChild())
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

        childRepo.deleteById(childID);
    }
}

