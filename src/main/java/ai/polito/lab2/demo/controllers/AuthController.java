package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.OnRegistrationCompleteEvent;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.EmailServiceImpl;
import ai.polito.lab2.demo.Service.IUserService;
import ai.polito.lab2.demo.User;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.RecoverVM;
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
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private IUserService service;

    @Autowired
    RoleRepo roleRepo;

    String regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public User registerUser(@RequestBody Register register, WebRequest request) {
        User u = userRepo.findByUsername(register.getUsername());
        if (u != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already registered"); //vabene così tanto non ci ha chiesto di restituirgli qualcosa, l'importante è che non si registri
        if (!register.getPassword().equals(register.getConfirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password and confirm password are different");
        if (!register.getPassword().matches(regex))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password too weak");

        u = this.userRepo.save(User.builder()
                .username(register.getUsername())
                .password(this.passwordEncoder.encode(register.getPassword()))
                .roles(Arrays.asList(roleRepo.findByRole("ROLE_USER")))
                .isEnabled(false) //quando l'utente conclude la registrazione via mail questo deve passare a true
                .build()
        );

        try {
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                    (u, request.getLocale(), appUrl));
            return u;
        } catch (Exception me) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Some problems occurred when sending the email", me);
        }

        //TODO decidere come agire se l'email non viene inviata,perchè al momento l'utente è registrato ma non è verificato
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

    @RequestMapping(value = "/confirm/{randomUUID}", method = RequestMethod.GET)
    public void confirm(@PathVariable String randomUUID) {
        User user = service.getUserByUUID(randomUUID);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }
        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already active");
        }
        Calendar cal = Calendar.getInstance();
        if ((user.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired token");
        }

        user.setEnabled(true);
        userRepo.save(user);
        throw new ResponseStatusException(HttpStatus.OK, "OK");
    }

    @RequestMapping(value = "/recover", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity recoverPassword(@RequestBody User username, WebRequest request) {

        User user = userRepo.findByUsername(username.getUsername());
        if (user == null) {
            //do something
        }
        String token = UUID.randomUUID().toString();
        service.createPasswordResetTokenForUser(user, token);
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
