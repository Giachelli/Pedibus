package ai.polito.lab2.demo.controllers;


import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.viewmodels.ChildRegisterVM;
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
public class ChildController {

    @Autowired
    private ChildRepo childRepo;


    @RequestMapping(value = "/register/child", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Child registerChild(@RequestBody ChildRegisterVM data) {


        Child child = Child.builder()
                .nameChild(data.getNameChild())
                .userID(data.getUserID())
                .build();

        System.out.println(data.getUserID());

        childRepo.save(child);
        return child;
    }

    @RequestMapping(value = "/user/{userID}/children", method = RequestMethod.GET)
    public ResponseEntity getMyChilds(@PathVariable ObjectId userID){

        ArrayList<Child> children = childRepo.findChildByUserID(userID);

        ArrayList<String> childrenName = new ArrayList<>();
        for (Child r : children) {
            childrenName.add(r.getNameChild());
        }

        Map<Object, Object> model = new HashMap<>();
        model.put("children", childrenName);
        return ok(model);
    }
}

