package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.UserDTO;
import ai.polito.lab2.demo.Entity.Route;
import ai.polito.lab2.demo.Repositories.RoleRepo;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.controllers.ReservationController;
import ai.polito.lab2.demo.viewmodels.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.util.*;

@Service
public class UserService implements IUserService {

    private static final int EXPIRATION = 30000;


    @Autowired
    RoleRepo roleRepository;

    @Autowired
    private RouteService routeService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    MessageService messageService;

    Logger logger = LoggerFactory.getLogger(UserService.class);


    public void saveUser(UserDTO userDTO) {

        User user = User.builder().
                username(userDTO.getEmail()).
                roles(userDTO.getRoles()).
                token(userDTO.getToken()).
                expiryDate(userDTO.getExpiryDate()).
                isEnabled(false).build();

        User u = userRepo.findByUsername(user.getUsername());
        if (u != null) /// da vedere cosa ritorna
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already registered"); //vabene così tanto non ci ha chiesto di restituirgli qualcosa, l'importante è che non si registri

        this.userRepo.save(user);

    }

    @Override
    public User getUserByUUID(String UUID) {
        return userRepo.findByToken(UUID);
    }

    @Override
    public User getUserByPassUUID(String UUID) {
        return userRepo.findByPasstoken(UUID);
    }

    @Override
    public UserDTO getUserDTOBy_id(ObjectId userID) {
        User u = userRepo.findUserBy_id(userID);
        UserDTO userDTO = u.convertToDTO();
        return userDTO;
    }

    @Override
    public User getUserBy_id(ObjectId userID) {
        User u = userRepo.findUserBy_id(userID);
        return u;
    }

    @Override
    public void changePassword(User user, String password) {

        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }

    @Override
    public boolean userEnabled(String username) {
        return userRepo.findByUsername(username).isEnabled();
    }

    @Override
    public void saveUser(User u) {
        userRepo.save(u);
    }

    @Override
    public boolean getVerificationToken(String randomUUID) {
        User user = this.getUserByUUID(randomUUID);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: User not found");
        }
        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already active");
        }
        Calendar cal = Calendar.getInstance();
        // in questo caso l'admin deve mandare di nuovo una mail perchè è scaduto il token
        if ((user.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired token");
        }
        return true;
    }

    public boolean getVerificationPassToken(String randomUUID) {
        User user = this.getUserByPassUUID(randomUUID);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: User not found");
        }
        Calendar cal = Calendar.getInstance();
        // in questo caso l'admin deve mandare di nuovo una mail perchè è scaduto il token
        if ((user.getExpiry_passToken().getTime() - cal.getTime().getTime()) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired token");
        }
        return true;
    }

    /**
     * funzione che serve dopo che l'utente ha inserito i dati per andare a confermare il tutto
     * @param randomUUID stringa random associata all'utente in fase di creazione
     * @param userVM dati inseriti dall'utente
     * @return come viene salvato lo user sul db
     */
    @Override
    public boolean manageUser(String randomUUID, ConfirmUserVM userVM) {

        User user = getUserByUUID(randomUUID);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: User not found");
        }
        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already active");
        }

       /* Calendar cal = Calendar.getInstance();
        // in questo caso l'admin deve mandare di nuovo una mail perchè è scaduto il token
        if ((user.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired token");
        }
        */
        user.setFamily_name(userVM.getFamily_name());
        user.setPassword(this.passwordEncoder.encode(userVM.getPassword()));
        user.setToken(null);
        user.setExpiryDate(null);
        user.setEnabled(true);
        userRepo.save(user);
        String action = "Utente creato";
        long day = new Date().getTime();
        messageService.createMessageNewUser(user.get_id(),
                getUserByUsername("admin@info.it").get_id(),
                action,
                day
        );
        return true;
    }

    @Override
    public UserDTO getUserDTOByUsername(String name) {
        User u = userRepo.findUserByUsername(name);
        UserDTO userDTO = u.convertToDTO();
        return userDTO;
    }

    @Override
    public User getUserByUsername(String name) {
        User u = userRepo.findUserByUsername(name);
        return u;
    }

    @Override
    public List<UserVM> getAllUser() {

        List<User> users = userRepo.findAll();
        ArrayList<String> usersIDString = new ArrayList<>();
        ArrayList<UserVM> userVMS = new ArrayList<>();

        for (User u : users) {
            UserVM userVM = UserVM.builder()
                    .userID(u.get_id().toString())
                    .username(u.getUsername())
                    .family_name(u.getFamily_name())
                    .availabilityVM((u.getAvailability()))
                    .isEnabled(u.isEnabled())
                    .build();
            usersIDString.add(u.get_id().toString());
            //System.out.println(u.get_id().toString());
            userVMS.add(userVM);
        }

        return userVMS;
    }

    /**
     * funzione per disabilitare l'utente
     * @param userID id dell'utente da disabilitare
     */
    @Override
    public void disableUser(ObjectId userID) {
        User u = this.getUserBy_id(userID);
        u.setEnabled(false);
        this.saveUser(u);
    }

    /**
     * funzione per abilitare l'utente
     * @param userID id dell'utente da abilitare
     */
    @Override
    public void ableUser(ObjectId userID) {
        User u = this.getUserBy_id(userID);
        u.setEnabled(true);
        this.saveUser(u);
    }

    @Override
    public UserRouteVM getRoutesUser(ObjectId userID) {

        User user = this.getUserBy_id(userID);
        ArrayList<Integer> adminRoute = new ArrayList<>();
        ArrayList<Integer> muleRoute = new ArrayList<>();
        if (user.getAdminRoutesID() != null)
            for (int i : user.getAdminRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error here!! Route non esistente");
                }
                adminRoute.add(r.getId());
            }
        if (user.getMuleRoutesID() != null)
            for (int i : user.getMuleRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error here!! Route non esistente");
                }
                muleRoute.add(r.getId());
            }

        return UserRouteVM.builder()
                .userID(user.get_id())
                .username(user.getUsername())
                .adminRoutes(adminRoute)
                .muleRoutes(muleRoute)
                .availability(user.getAvailability())
                .build();

    }

    /**
     * funzione che ritorna il view model per il login
     * @param username username dell'utente che si vuole loggare
     * @return informazioni dell'utente
     */
    @Override
    public LoginUserVM getUserLoginByUsername(String username) {
        User u = userRepo.findUserByUsername(username);



        return LoginUserVM.builder().userID(u.get_id().toString())
                .username(u.getUsername())
                .roles(u.getRolesString())
                .adminMules(u.getMuleRoutesID())
                .family_name(u.getFamily_name())
                .adminRoutes(u.getAdminRoutesID())
                .andataStop(u.getUserVMMapStop(u.getAndataStops()))
                .ritornoStop(u.getUserVMMapStop(u.getRitornoStops()))
                .childsNumber(u.getChildsID().size())
                .build();
    }

    @Override
    public void editUser(ObjectId userID, modifyRoleUserVM modifyRoleUser) throws Exception {
        User user = this.getUserBy_id(userID);
        ArrayList<Integer> adminRoutes = modifyRoleUser.getAdminRoutes();
        ArrayList<Integer> muleRoutes = modifyRoleUser.getMuleRoutes();

        ArrayList<Boolean> oldAvailability = user.getAvailability();

        ArrayList<Integer> adminBefore = new ArrayList<>();
        ArrayList<Integer> muleBefore = new ArrayList<>();


        Set<Integer> adminRouteID = new HashSet<>();
        Set<Integer> muleRouteID = new HashSet<>();

        if (user.getAdminRoutesID() != null) // lo user è admin di qualche linea
            for (int i : user.getAdminRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    String error = "Errore nella modify User passo un id route non esistente";
                    logger.error(error);
                    throw new Exception(error);
                    //return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                if(r.getUsernameAdmin().contains(user.getUsername())) {
                    r.removeAdmin(user.getUsername());
                    adminBefore.add(i);
                }
                else
                {
                    throw  new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Settaggio db errore");
                }
                routeService.saveRoute(r);
            }

        if (user.getMuleRoutesID() != null)
            for (int i : user.getMuleRoutesID()) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    String error = "Errore nella modify USer passo un id route non esistente";
                    logger.error(error);
                    throw new Exception(error);
                   //return new ResponseEntity(HttpStatus.BAD_REQUEST);

                }

                if(r.getUsernameMule().contains(user.getUsername())) {
                    r.removeMule(user.getUsername());
                    muleBefore.add(i);
                }else
                {
                    throw new Exception("Errore nel precaricamento file");
                }

                routeService.saveRoute(r);
            }


        //Check if the array of integers passed with the request is empty (admin routes id case)
        if (adminRoutes.size() == 0) {
            //if the array is empty = the user isn't admin for any routes and we delete the role "admin" from his role list
            if (user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN"))){
                user.removeRole(roleRepository.findByRole("ROLE_ADMIN"));
            }

        } else {
            //add the id routes in the user list
            //todo vedere se ci sono idee migliori
            for (int i : adminRoutes) {
                Route r = routeService.getRoutesByID(i);
                if (r == null) {
                    String error = "Errore nella modify USer passo un id non esistente";
                    logger.error(error);
                    throw new Exception(error);
                    //return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                // controllo che non sia già Admin per la linea
                adminRouteID.add(i);
                Route addAdminRoute = routeService.getRoutesByName(r.getNameR());
                if(addAdminRoute.getUsernameAdmin() == null)
                {
                    addAdminRoute.addAdmin(user.getUsername());
                    routeService.saveRoute(addAdminRoute);
                }
                if (!addAdminRoute.getUsernameAdmin().contains(user.getUsername())) {
                    addAdminRoute.addAdmin(user.getUsername());
                    routeService.saveRoute(addAdminRoute);
                }
            }
            if (adminRoutes.size() > 0)
                if (!user.getRoles().contains(roleRepository.findByRole("ROLE_ADMIN")))
                    user.addRole(roleRepository.findByRole("ROLE_ADMIN"));
        }

        user.setAdminRoutesID(adminRouteID);
        this.saveUser(user);

        //Check if the array of integers passed with the request is empty (mule routes id case)
        if (muleRoutes.size() == 0) {
            //the same of admin cases
            if (user.getRoles().contains(roleRepository.findByRole("ROLE_MULE"))){
                user.removeRole(roleRepository.findByRole("ROLE_MULE"));
            }
        } else {
            for (int j : muleRoutes) {
                Route r = routeService.getRoutesByID(j);
                if (r == null) {
                    String error = "Errore nella modify USer passo un id route non esistente";
                    logger.error(error);
                    throw new Exception(error);
                    //return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                // controllo che non sia già Mule per la linea

                muleRouteID.add(j);
                Route addMuleRoute = routeService.getRoutesByName(r.getNameR());
                if (addMuleRoute.getUsernameMule() == null)
                {
                    addMuleRoute.addMule(user.getUsername());
                    routeService.saveRoute(addMuleRoute);
                }
                if (!addMuleRoute.getUsernameMule().contains(user.getUsername()) ) {
                    addMuleRoute.addMule(user.getUsername());
                    routeService.saveRoute(addMuleRoute);
                }
            }

            if (muleRoutes.size() > 0)
                if (!user.getRoles().contains(roleRepository.findByRole("ROLE_MULE"))) {
                    user.addRole(roleRepository.findByRole("ROLE_MULE"));
                }

        }
        user.setMuleRoutesID(muleRouteID);
        user.setAvailability(modifyRoleUser.getAvailability());
        user.setAndataStops(modifyRoleUser.getStopAndata());
        user.setRitornoStops(modifyRoleUser.getStopRitorno());
        this.saveUser(user);

        long day = new Date().getTime();
        HashMap<Integer,ArrayList<String>> otherAdmins = new HashMap<Integer, ArrayList<String>>(); // mappa che contiene le linee a cui non sarò più admin e i relativi admin
        ArrayList<Integer> old_newRoute = new ArrayList<>();
        old_newRoute.addAll(adminBefore);
        old_newRoute.addAll(adminRoutes);
        old_newRoute.addAll(muleBefore);
        old_newRoute.addAll(muleRoutes);

        /* messaggio che deve arrivare agli admin di linea */

        for (Integer i : old_newRoute){
            if(otherAdmins.containsKey(i)) //evito di mandare due notifiche agli admin delle linee che rimangono invariate
                continue;
            Route r = routeService.getRoutesByID(i);
            if (r.getUsernameAdmin()!=null && r.getUsernameAdmin().size()!=0){
                for ( String s : r.getUsernameAdmin()){
                    if (s.equals(user.getUsername()))
                        continue;
                    if (otherAdmins.containsKey(i)) //entra dalla seconda volta in poi
                        otherAdmins.get(i).add(s);
                    else{ // entra la prima volta
                        otherAdmins.put(i,new ArrayList<String>());
                        otherAdmins.get(i).add(s);
                    }
                }
            }
        }
        for (Map.Entry<Integer,ArrayList<String>> entry: otherAdmins.entrySet()){
            String action = "I privilegi/disponibilità relativi allo user " + user.getUsername() + " aggiornati.";
            // mettere controllo che se entry.getValue è uguale al sender, allora il messaggio non va inviato
            if (entry.getValue().contains(modifyRoleUser.getModifiedBy())){
                entry.getValue().remove(modifyRoleUser.getModifiedBy());  // in questo modo il messaggio non dovrebbe arrivare a chi ha fatto l'operazione anche se admin di un altra linea per cui lo user ha subito delle variazioni
            }
            messageService.createMessageNewRolesOtherAdmins(modifyRoleUser.getModifiedBy(),
                    entry.getValue(),
                    action,
                    day,
                    entry.getKey());

            /* se anche gli admin posso cambiare le availability di un mule allora ritornare su questo messaggio, poiché lo user non viene informato*/

                /* Da utilizzare solo nel caso vogliamo notificare che uno user ha modificato le sue dispo, sennò no.
                if (!(oldAvailability.equals(modifyRoleUser.getAvailability()))){
                    String action1= "Modifica disponibilità";
                    messageService.createMessageEditAvailability(modifyRoleUser.getModifiedBy(),
                            entry.getValue(),
                            action1,
                            day,
                            entry.getKey());
                }
                */


        }

        /* seconda parte che riguarda lo user stesso */
        if (!(modifyRoleUser.getModifiedBy().equals(this.getUserBy_id(user.get_id()).getUsername()))){
            String action= "Privilegi aggiornati";
            messageService.createMessageNewRoles(modifyRoleUser.getModifiedBy(),
                    user.get_id(),
                    action,
                    day,
                    adminRoutes,
                    muleRoutes);
        }

    }

    /*@Override
    public ArrayList<UserDTO> findAll() {
        ArrayList<User> users = userRepo.findAll();
        ArrayList<UserDTO> userDTOArrayList = new ArrayList<>();
        for (User user : users) {
            UserDTO userDTO = user.convertToDTO();
            userDTOArrayList.add(userDTO);
        }
        return userDTOArrayList;
    }*/

    @Override
    public List<User> findAll(){
        return userRepo.findAll();
    }

    @Override
    public boolean deleteUserbyID(ObjectId userID) {
        userRepo.deleteById(userID);
        return true;
    }

    public void updateUser(UserDTO newAdmin) {
        User user = User.builder().
                username(newAdmin.getEmail()).
                roles(newAdmin.getRoles()).
                token(newAdmin.getToken()).
                expiryDate(newAdmin.getExpiryDate()).
                isEnabled(false).build();

        this.userRepo.save(user);

    }

    /*public void createPasswordResetTokenForUser(User user, String token) {

        user.setPasstoken(token);
        user.setExpiry_passToken(user.calculateExpiryDate(EXPIRATION));

        String recipientAddress = user.getUsername();
        String subject = "Request Change Password";
        String confirmationUrl = "/recover/" + token;
        String message = "Questa mail serve per cambiare password. Clicca sul token";

        emailService.sendSimpleMessage(recipientAddress,subject, message + " " + "http://localhost:8080" + confirmationUrl); //non è troppo scalabile, vedere meglio come si fa
        userRepo.save(user);
    }*/


}
