package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.OnRecoverCompleteEvent;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.OnRegistrationCompleteEvent;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.IUserService;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.viewmodels.AuthenticationRequestVM;
import ai.polito.lab2.demo.viewmodels.ConfirmUserVM;
import ai.polito.lab2.demo.viewmodels.RecoverVM;
import ai.polito.lab2.demo.viewmodels.RegisterVM;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.ResponseEntity.badRequest;
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
    private IUserService userService;


    String regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_])(?=\\S+$).{8,}";

    /**
     *
     * @param register Oggetto fornito con i campi necessari per la registrazione dello user
     * @param request
     * @return
     */
    @Secured("ROLE_SYSTEM_ADMIN")
    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Enpoint per la registrazione")
    public ResponseEntity registerUser(@RequestBody RegisterVM register, WebRequest request) {

        if (userService.getUserByUsername(register.getEmail())!=null){
            return new ResponseEntity("Lo user è già presente",HttpStatus.BAD_REQUEST );
        }

        UserDTO user = UserDTO.builder().
                email(register.getEmail()).
                roles(Arrays.asList(roleRepo.findByRole(register.getRole()))).build();


        try {
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                    (user, request.getLocale(), appUrl));
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception me) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Some problems occurred when sending the email", me);
        }

    }

    /**
     *
     * @param data oggetto con i campi necessari per effettuare il login
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Endpoin per effettuare il login")
    public ResponseEntity signin(@RequestBody AuthenticationRequestVM data) {
        try {
            System.out.println("Post /login");
            String username = data.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            if (!userService.userEnabled(username)) {
                throw new BadCredentialsException("You are not enabled to login");
            }
            String token = jwtTokenProvider.createToken(username, this.userRepo.findByUsername(username).getRolesString());

            User u = userService.getUserByUsername(username);

            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            model.put("roles", u.getRolesString());
            model.put("family_name", u.getFamily_name());
            model.put("adminRoutes", u.getAdminRoutesID());
            model.put("muleRoutes", u.getMuleRoutesID());
            System.out.println("User: " + username + " is logged");
            return ok(model);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username/password supplied"); //deve restituire 401 Unauthorized, lo vedo io
        }
    }

    //TODO fare la get da inviare tramite mail

    /**
     *
     * @param randomUUID token fornito per completare la registrazione
     * @return
     */
    @RequestMapping(value = "/confirm/{randomUUID}", method = RequestMethod.GET)
    @ApiOperation("Endpoint per raggiungere la pagina di completamento iscrizione")
    public ResponseEntity getPage(@PathVariable String randomUUID) {
        if (userService.getVerificationToken(randomUUID)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, " NOT FOUND");
        }

    }

    /**
     *
     * @param randomUUID token passatogli
     * @param userVM
     * @return
     */
    @RequestMapping(value = "/confirm/{randomUUID}", method = RequestMethod.POST)
    @ApiOperation("Endpoint per la creazione dell'utente dopo che ha inserito i dati")
    public ResponseEntity confirm(@PathVariable String randomUUID, @RequestBody ConfirmUserVM userVM) {
        if (!userService.getVerificationToken(randomUUID))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token non valido");

        if ((userVM.getPassword().length())<8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password corta");

        if (!userVM.getPassword().equals(userVM.getConfirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password are differents");

        if (!userVM.getPassword().matches(regex)) {
            return new ResponseEntity("Password non ha tutti i caratteri richiesti",HttpStatus.BAD_REQUEST );
        }

        if (userService.manageUser(randomUUID, userVM)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, " NOT FOUND");
        }
    }

    /**
     *
     * @param email con cui ci si è registrati
     * @param request
     * @return
     */
    @RequestMapping(value = "/recover", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Endpoint per mandare la mail per il recupero della password")
    public ResponseEntity recoverPassword(@RequestBody String email, WebRequest request) {

        User user = userRepo.findByUsername(email);
        if (user == null) {
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


        try {
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRecoverCompleteEvent
                    (user, request.getLocale(), appUrl));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception me) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Some problems occurred when sending the email", me);
        }
    }

    /**
     *
     * @param randomUUID token utilizzato per il recover della password
     * @param vm corpo della richiesta
     * @return
     */
    @RequestMapping(value = "/recover/{randomUUID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity processRecoverPassword(@PathVariable String randomUUID, RecoverVM vm) {
        if (userService.getVerificationPassToken(randomUUID)) {

            System.out.println("RECOVER REQUEST");
            User user = userService.getUserByPassUUID(randomUUID);
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
            userService.changePassword(user, vm.getPass());
            System.out.println("impostata " + user.getPassword());

            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, " NOT FOUND");
        }
    }
}
