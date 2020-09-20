package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ChildDTO;
import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import ai.polito.lab2.demo.viewmodels.ChildAllVM;
import ai.polito.lab2.demo.viewmodels.ChildVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChildServiceImpl implements ChildService {

    @Autowired
    private ChildRepo childRepo;

    @Autowired
    private RouteService routeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @Autowired
    private StopService stopService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MongoTemplate mongoTemplate;




    // ricerca un Child tramite ID
    @Override
    public ChildDTO findChildDTObyID(ObjectId childId) {
        Child child = childRepo.findChildByChildID(childId);
        //return child.convertDTO();
        return null;
    }

    @Override
    public Child findChildbyID(ObjectId childId) {
        Child child = childRepo.findChildByChildID(childId);
        return child;
    }

    // questa funzione ritorna tutti i figli per un utente specifico ( controllare nel caso
    //ritorni zero figli) --> capiterà mai?
    @Override
    public ArrayList<ChildDTO> findChildbyUsername(String username) {
        ArrayList<Child> childs = childRepo.findChildByUsername(username);
        ArrayList<ChildDTO> childDTOS = new ArrayList<>();
        if (childs.isEmpty()) {
            ChildDTO c = ChildDTO.builder().build();
            childDTOS.add(c);
        } else {
            for (Child c : childs) {
                //childDTOS.add(c.convertDTO());
            }

        }
        return childDTOS;
    }

    // ritorna i figli in base al cognome: serve? Forse per velocizzare qualche ricerca?
    @Override
    public ChildDTO findChildbyFamilyName(String familyName) {
        return null;
    }

    // salva un child sul db, trasformando prima il DTO in Entity
    @Override
    public void saveChild(ChildDTO childDTO, ObjectId familyID) {
        Child c = childDTO.convert(familyID);
        childRepo.save(c);

    }

    public Child findChildByNameChildAndUsername(String nameChild, String username){
        Query query = new Query();
        query.addCriteria(Criteria.where("nameChild").is(nameChild).and("username").is(username));
        List<Child> child= mongoTemplate.find(query, Child.class);
        if (child!= null && child.size()!= 0){
            return child.get(0);
        }
        else
            return null;
    }

    public ChildVM registerChild(ChildVM data){
        int routeID = 0;
        Child child;
        RouteDTO r;
        String stopName;

        /* Controllo per vedere se stiamo inserendo un bambino con lo stesso nome */
        if (findChildByNameChildAndUsername(data.getNameChild(),data.getUsername())!=null) {
            return null;
        }

        if(data.getNameRoute()!= null && !data.getNameRoute().isEmpty() && data.getDirection() != null && data.getStopID() != null) {
            /* caso in cui sia stata messa solo l'andata o il ritorno */
            if (data.getNameRoute().contains("")) {
                int i = 0;
                for (String s : data.getNameRoute()) {
                    if (!s.equals("")) {
                        routeID = routeService.findIDRouteByNameR(data.getNameRoute().get(i));
                        r = routeService.findRouteByNameR(data.getNameRoute().get(i));
                        break;
                    }
                    i++;
                }
                child = Child.builder()
                        .nameChild(data.getNameChild())
                        .username(data.getUsername())
                        .family_name(data.getFamily_name())
                        .isMale(data.isMale())
                        .color(data.getColor())
                        .direction(data.getDirection())
                        .stopID(data.getStopID())
                        .nameRoute(data.getNameRoute())
                        .build();
                childRepo.save(child);

                // child = childRepo.findChildByNameChildAndUsername(data.getNameChild(), data.getUsername());
                if (data.getDirection() != null && !data.getDirection().equals("")) {
                    if (data.getDirection().equals("andata") || data.getDirection().equals("ritorno")) {
                        // TODO per ora lo faccio per due giorni ma è da fare per tutto il periodo scolastico
                        // Usare Calendar che permette tramite get(Day_of_the_week) di prendere la data corretta
                        TimeZone timeZone = TimeZone.getTimeZone("UTC");
                        Calendar today = Calendar.getInstance(timeZone);
                        today.set(Calendar.MILLISECOND, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        int day = 0;
                        long dataTimeStamp;
                        int j = 200;
                        boolean correct = false;
                        int offsetDayOne = reservationService.calculateFirstDay();
                        for (int k = 0; k < j; k++) {
                            if (!correct) {
                                if (offsetDayOne > 0) {
                                    today.add(Calendar.DATE, offsetDayOne);
                                    correct = true;
                                } else {
                                    j = j + offsetDayOne;
                                    today.add(Calendar.DATE, 1);
                                    correct = true;
                                }
                            }
                            day = today.get(Calendar.DAY_OF_WEEK);
                            dataTimeStamp = today.getTimeInMillis();
                            if (day == 1 || day == 7) {
                                today.add(Calendar.DATE, 1);
                                j++;
                                continue;
                            }
                            Reservation reservation = Reservation.builder()
                                    .childID(child.getChildID())
                                    .familyName(child.getFamily_name())
                                    .date(dataTimeStamp)
                                    .direction(data.getDirection())
                                    .name_route(data.getNameRoute().get(i))
                                    .routeID(routeID)
                                    .stopID(new ObjectId(data.getStopID().get(i)))
                                    .booked(true)
                                    .inPlace(false)
                                    .build();

                            reservationService.save(reservation);
                            // aggiungere tramite calendar con set + 1
                            today.add(Calendar.DATE, 1);
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }else if(!data.getNameRoute().contains("") && data.getNameRoute().size()==2){
                if (data.getDirection().equals("entrambi")) {
                    System.out.println("COMUNQUE QUI ENTRO");
                    child = Child.builder()
                            .nameChild(data.getNameChild())
                            .username(data.getUsername())
                            .family_name(data.getFamily_name())
                            .isMale(data.isMale())
                            .color(data.getColor())
                            .direction(data.getDirection())
                            .stopID(data.getStopID())
                            .nameRoute(data.getNameRoute())
                            .build();
                    childRepo.save(child);
                    for (int k = 0; k < data.getNameRoute().size(); k++) {
                        routeID = routeService.findIDRouteByNameR(data.getNameRoute().get(k));
                        r = routeService.findRouteByNameR(data.getNameRoute().get(k));
                        TimeZone timeZone = TimeZone.getTimeZone("UTC");
                        Calendar today = Calendar.getInstance(timeZone);
                        today.set(Calendar.MILLISECOND, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        int day = 0;
                        long dataTimeStamp;
                        int j = 200;
                        boolean correct = false;
                        int offsetDayOne = reservationService.calculateFirstDay();
                        for (int h = 0; h < j; h++) {
                            if (!correct) {
                                if (offsetDayOne > 0) {
                                    today.add(Calendar.DATE, offsetDayOne);
                                    correct = true;
                                } else {
                                    j = j + offsetDayOne;
                                    today.add(Calendar.DATE, 1);
                                    correct = true;
                                }
                            }
                            day = today.get(Calendar.DAY_OF_WEEK);
                            dataTimeStamp = today.getTimeInMillis();
                            if (day == 1 || day == 7) {
                                today.add(Calendar.DATE, 1);
                                j++;
                                continue;
                            }if (k==1){
                                Reservation reservation = Reservation.builder()
                                        .childID(child.getChildID())
                                        .familyName(child.getFamily_name())
                                        .date(dataTimeStamp)
                                        .direction("ritorno")
                                        .name_route(data.getNameRoute().get(k))
                                        .routeID(routeID)
                                        .stopID(new ObjectId(data.getStopID().get(k)))
                                        .booked(true)
                                        .inPlace(false)
                                        .build();

                                reservationService.save(reservation);
                                // aggiungere tramite calendar con set + 1
                                today.add(Calendar.DATE, 1);
                            }else{
                                Reservation reservation = Reservation.builder()
                                        .childID(child.getChildID())
                                        .familyName(child.getFamily_name())
                                        .date(dataTimeStamp)
                                        .direction("andata")
                                        .name_route(data.getNameRoute().get(k))
                                        .routeID(routeID)
                                        .stopID(new ObjectId(data.getStopID().get(k)))
                                        .booked(true)
                                        .inPlace(false)
                                        .build();

                                reservationService.save(reservation);
                                // aggiungere tramite calendar con set + 1
                                today.add(Calendar.DATE, 1);
                            }
                        }
                    }
                }else{
                    return null;
                }

            }else{
                return null;
            }
        }else{ // caso in cui il bambino è iscritto senza fermata e linea di default
            child = Child.builder()
                    .nameChild(data.getNameChild())
                    .username(data.getUsername())
                    .family_name(data.getFamily_name())
                    .isMale(data.isMale())
                    .color(data.getColor())
                    .build();
            childRepo.save(child);
//            child = childRepo.findChildByNameChildAndUsername(data.getNameChild(), data.getUsername());

        }
        String action = "Bambino creato";
        long day = new Date().getTime();
        messageService.createMessageResp(userService.getUserByUsername(child.getUsername()).get_id(),
                userService.getUserByUsername("giacomo.chelli4@gmail.com").get_id(),
                child.getChildID(),
                action,
                day,
                "messageChildCreation"
        );


        ChildVM data_return = data;
        data_return.setChildID(child.getChildID().toString());
        System.out.println("Arrivo qui e ed esco");
        return data_return;
    };

    public ArrayList<ChildVM> getMyChildren(String username){

        HashMap<Integer,ArrayList<String>> stopName = new HashMap<>();
        ArrayList<String> stopName1 = new ArrayList<String>();
        ArrayList<Child> children ;
        Integer count_bimbi = 0;
        ArrayList<ChildVM> childrenVM = new ArrayList<>();

        if (userService.getUserByUsername(username)==null){
            return null;
        }else{
            children = childRepo.findChildByUsername(username);
        }
        for (Child r : children) {
            stopName1.clear();
            Reservation reservation= reservationService.findRecentReservation(r.getChildID(),new Date().getTime());
            if (r.getStopID()!=null && r.getNameRoute()!=null && r.getDirection()!=null){
                if ( r.getNameRoute().contains("")){
                    int i = 0;
                    for (String s : r.getNameRoute()) {
                        if (!s.equals("")) {
                            stopName1.add(stopService.findStopbyId(new ObjectId(r.getStopID().get(i))).getNome());
                            break;
                        }
                        i++;
                    }
                    stopName.put(count_bimbi,new ArrayList<>(stopName1));
                    if (r.getDirection().equals("andata") || r.getDirection().equals("ritorno")){
                        childrenVM.add(
                                ChildVM.builder()
                                        .childID(r.getChildID().toString())
                                        .nameChild(r.getNameChild())
                                        .family_name(r.getFamily_name())
                                        .color(r.getColor())
                                        .username(r.getUsername())
                                        .isMale(r.isMale())
                                        .direction(r.getDirection())
                                        .nameRoute(r.getNameRoute())
                                        //      .nextCorsa(reservation.getName_route() + reservation.getStopID() + reservation.getDirection())
                                        .stopID(r.getStopID())
                                        .stopName(stopName.get(count_bimbi))
                                        .build()
                        );
                        System.out.println("stop name del child" + childrenVM.get(count_bimbi).getStopName());
                    }
                }else if(r.getDirection().equals("entrambi")){
                    for (int i = 0; i<r.getStopID().size(); i++){
                        stopName1.add(stopService.findStopbyId(new ObjectId(r.getStopID().get(i))).getNome());
                    }
                    stopName.put(count_bimbi,new ArrayList<>(stopName1));
                    childrenVM.add(
                            ChildVM.builder()
                                    .childID(r.getChildID().toString())
                                    .nameChild(r.getNameChild())
                                    .family_name(r.getFamily_name())
                                    .color(r.getColor())
                                    .username(r.getUsername())
                                    .isMale(r.isMale())
                                    .direction(r.getDirection())
                                    .nameRoute(r.getNameRoute())
                                    //      .nextCorsa(reservation.getName_route() + reservation.getStopID() + reservation.getDirection())
                                    .stopID(r.getStopID())
                                    .stopName(stopName.get(count_bimbi))
                                    .build()
                    );
                }

            }else{
                childrenVM.add(
                        ChildVM.builder()
                                .childID(r.getChildID().toString())
                                .nameChild(r.getNameChild())
                                .family_name(r.getFamily_name())
                                .color(r.getColor())
                                //   .nextCorsa(reservation.getName_route() + reservation.getStopID() + reservation.getDirection())
                                .username(r.getUsername())
                                .isMale(r.isMale())
                                .build());
            }
            count_bimbi++;
        }
        return childrenVM;
    }

    @Override
    public ArrayList<Child> findAllChild() {
        return (ArrayList<Child>) childRepo.findAll();
    }

    @Override
    public ArrayList<ChildAllVM> findAllChildren() {
        ArrayList<Child> children = childRepo.findAll();
        ArrayList<ChildAllVM> childrenVM = new ArrayList<>();

        for (Child c : children) {
            childrenVM.add(
                    ChildAllVM.builder()
                            .childID(c.getChildID().toString())
                            .nameChild(c.getNameChild())
                            .family_name(c.getFamily_name())
                            .isMale(c.isMale())
                            .username(c.getUsername())
                            .build()
            );
        }
        return childrenVM;
    }

    public void deleteChild(ObjectId childID){
        ArrayList<ObjectId> reservations_id = new ArrayList<>();
        for (Reservation r:reservationService.findReservationByChildID(childID)){

            reservations_id.add(r.getId());
        }

        reservations_id.forEach( (x) -> {
            reservationService.delete(x);
        });

        String action = "Bambino precedentemente creato cancellato";
        long day = new Date().getTime();

        Child child = childRepo.findChildByChildID(childID);
        messageService.createMessageResp(userService.getUserByUsername(child.getUsername()).get_id(),
                userService.getUserByUsername("giacomo.chelli4@gmail.com").get_id(),
                child.getChildID(),
                action,
                day,
                "messageChildDelete"
        );
        childRepo.deleteById(childID);
    }

}
