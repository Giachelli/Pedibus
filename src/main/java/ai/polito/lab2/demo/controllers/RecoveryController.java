package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.viewmodels.RecoverVM;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RecoveryController {

    @GetMapping("/recover/{randomUUID}")
    public String recover (Model model, @PathVariable String randomUUID){
        model.addAttribute("message", "100");
        model.addAttribute("vm", new RecoverVM());
        return "recover";
    }

}
