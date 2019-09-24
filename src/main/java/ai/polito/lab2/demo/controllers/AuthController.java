package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.OnRegistrationCompleteEvent;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.IUserService;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.ConfirmUserVM;
import ai.polito.lab2.demo.viewmodels.RecoverVM;
import ai.polito.lab2.demo.viewmodels.RegisterVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.ResponseEntity.ok;

//@RequestMapping("/auth")
@RestController
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private IUserService service;


    String regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO registerUser(@RequestBody RegisterVM register, WebRequest request) {

        UserDTO user = UserDTO.builder().
                    email(register.getEmail()).
                    roles(Arrays.asList(roleRepo.findByRole(register.getRole()))).build();


        try {
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                    (user, request.getLocale(), appUrl));
            return user;
        } catch (Exception me) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Some problems occurred when sending the email", me);
        }

    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity signin(@RequestBody AuthenticationRequest data) {

        try {
            String username = data.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            String token = jwtTokenProvider.createToken(username, this.userRepo.findByUsername(username).getRolesString());

            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            return ok(model);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/password supplied"); //deve restituire 401 Unauthorized, lo vedo io
        }
    }

    @RequestMapping(value = "/confirm/{randomUUID}", method = RequestMethod.POST)
    public void confirm(@PathVariable String randomUUID, @RequestBody ConfirmUserVM userVM) {
        if(service.manageUser(randomUUID,userVM))
        {
        throw new ResponseStatusException(HttpStatus.OK, "OK");}
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND," NOT FOUND");
        }
    }

    @RequestMapping(value = "/recover", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity recoverPassword(@RequestBody User username, WebRequest request) {

        User user = userRepo.findByUsername(username.getUsername());
        if (user == null) {
            //do something
        }
        String token = UUID.randomUUID().toString();
        //service.createPasswordResetTokenForUser(user, token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/recover/{randomUUID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity processRecoverPassword(@PathVariable String randomUUID, @ModelAttribute("vm") RecoverVM vm) {
        //TO DO: CHECK TOKEN VALIDITY


        System.out.println("RECOVER REQUEST");
        User user = service.getUserByPassUUID(randomUUID);
        System.out.println("Pass user prima " + user.getPassword());
        if (user == null) {
            return new ResponseEntity<>(
                    "Errore 404 – Not found",//utente o token non validi",
                    HttpStatus.BAD_REQUEST);
        }
        if (!vm.getPass().equals(vm.getConfpass())) {
            return new ResponseEntity<>(
                    "Errore 404 – Not found",//utente o token non validi",
                    HttpStatus.BAD_REQUEST);
        }
        if (!vm.getPass().matches(regex)) {
            System.out.println("REGEX NOT MATCH");
            return new ResponseEntity<>(
                    "Errore 404 – Not found",//la password non soddisfa i requisiti minimi",
                    HttpStatus.BAD_REQUEST);
        } else {
            System.out.println("REGEX PASS");
        }
        System.out.println("prova ad impostare " + vm.getPass());

        //user.setPassword(b.encode(vm.getPass()));
        service.changePassword(user, vm.getPass());
        System.out.println("impostata " + user.getPassword());

        return new ResponseEntity<>(HttpStatus.OK);
    }
    //@RequestMapping(value = "/recover/{randomUUID}", method = RequestMethod.GET)
    //public String processRecover(@PathVariable String randomUUID, Model m, @ModelAttribute("vm") @Valid RecoverVM vm){
    //public String confirm() {
    //
    //    return "confirm";
    //}
}
