package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.Reservation;
import ai.polito.lab2.demo.Entity.Shift;
import ai.polito.lab2.demo.Entity.User;
import ai.polito.lab2.demo.Repositories.ShiftRepo;
import ai.polito.lab2.demo.viewmodels.ShiftCreateVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private ShiftRepo shiftRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;
    @Autowired
    private RouteService routeService;


    /**
     * salvataggio dello shift sul db
     * @param t shift Dto passato
     * @return ritorna lo shift creato lato db
     */
    @Override
    public Shift save(ShiftDTO t) {
        Shift shift = Shift.builder()
                .muleID(t.getMuleID())
                .AdminID(t.getAdminID())
                .date(t.getData())
                .lineaID(t.getLineId())
                .direction(t.isDirection())
                .status(t.getStatus())
                .startID(t.getStartShiftID())
                .stopID(t.getStopShiftID())
                .build();

        return shiftRepo.save(shift);
    }

    /**
     * cancellazione di uno shift
     * @param turnID id dello shift da eliminare
     */
    @Override
    public void delete(ObjectId turnID) {

        Shift shift = this.getTurnByID(turnID);

        String action = "Cancellazione turno";

        long day = new Date().getTime();

        ArrayList<ObjectId> receiversID = new ArrayList<>();

        receiversID.add(shift.getMuleID());

        for (String username : routeService.getRoutesByID(shift.getLineaID()).getUsernameAdmin()) {
            User u = userService.getUserByUsername(username);
            receiversID.add(u.get_id());
        }

        messageService.createMessageDeleteTurns(shift.getAdminID(),
                receiversID,
                action,
                day,
                shift.getTurnID()
        );

        shiftRepo.delete(this.getTurnByID(turnID));

    }

    /**
     * Ritorna lo shift richiesto
     * @param turnID id dello shift da ritornare
     * @return shift tramite l'id
     */
    @Override
    public Shift getTurnByID(ObjectId turnID) {
        return shiftRepo.getTurnByTurnID(turnID);
    }

    /**
     * modifica dello shift che viene salvato
     * @param t shift da salvare
     */
    @Override
    public void editTurn(Shift t) {
        shiftRepo.save(t);
    }

    /**
     * ritorna tutti gli shift per quel mule per la linea scelta
     * @param routeID id della linea
     * @param muleID id del mule
     * @return lista di shift
     */
    @Override
    public List<ShiftCreateVM> getTurns(int routeID, ObjectId muleID) {
        List<Shift> shifts = shiftRepo.findByLineaIDAndMuleID(routeID, muleID);

        ArrayList<ShiftCreateVM> listShifts = new ArrayList<>();
        for (Shift s : shifts) {
            ShiftCreateVM shiftCreateVM = ShiftCreateVM.builder()
                    .shiftId(s.getTurnID().toString())
                    .data(s.getDate())
                    .direction(s.isDirection())
                    .lineId(s.getLineaID())
                    .username(userService.getUserBy_id(s.getMuleID()).getUsername())
                    .usernameAdmin(userService.getUserBy_id(s.getAdminID()).getUsername())
                    .status(s.getStatus())
                    .startShiftId(s.getStartID().toString())
                    .stopShiftId(s.getStopID().toString())
                    .build();
            listShifts.add(shiftCreateVM);
        }
        return listShifts;
    }

    /**
     * ritorna tutti gli shift per una data per un mule per una certa direzione
     * @param mule username del mule
     * @param date data
     * @param dir andata o ritorno
     * @return
     */
    @Override
    public Shift getTurnByMuleDateDirection(String mule, long date, boolean dir) {
        User u = userService.getUserByUsername(mule);
        return shiftRepo.getTurnByMuleIDAndDateAndDirection(u.get_id(),date,dir);
    }

    /**
     * ritorna tutti gli shift per una certa linea
     * @param routeID id della linea
     * @return lista di shift da ritornare
     */
    @Override
    public List<ShiftCreateVM> getTurnsRoute(int routeID) {
        List<Shift> shifts = shiftRepo.findByLineaID(routeID);
        ArrayList<ShiftCreateVM> listShifts = new ArrayList<>();
        for (Shift s : shifts) {
            ShiftCreateVM shiftCreateVM = ShiftCreateVM.builder()
                    .shiftId(s.getTurnID().toString())
                    .data(s.getDate())
                    .direction(s.isDirection())
                    .lineId(s.getLineaID())
                    .username(userService.getUserBy_id(s.getMuleID()).getUsername())
                    .usernameAdmin(userService.getUserBy_id(s.getAdminID()).getUsername())
                    .status(s.getStatus())
                    .startShiftId(s.getStartID().toString())
                    .stopShiftId(s.getStopID().toString())
                    .build();
            listShifts.add(shiftCreateVM);
        }
        return listShifts;
    }

    /**
     * Ritorna tutti gli shift per una certa linea per un mule nei tempi recenti e futuri
     * @param routeID id della linea
     * @param muleID id del mule
     * @return lista di shift trovata
     */
    @Override
    public List<ShiftCreateVM> getTurnsDate(int routeID, ObjectId muleID) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        /**
         * ricerca nel db degli shift per il mule selezionato nella linea selezionata da una settimana fa ad oggi
         */
        List<Shift> shifts = shiftRepo.findByLineaIDAndMuleIDAndDateAfter(routeID, muleID, cal.getTimeInMillis());

        ArrayList<ShiftCreateVM> listShifts = new ArrayList<>();
        for (Shift s : shifts) {
            ShiftCreateVM shiftCreateVM = ShiftCreateVM.builder()
                    .shiftId(s.getTurnID().toString())
                    .data(s.getDate())
                    .direction(s.isDirection())
                    .lineId(s.getLineaID())
                    .username(userService.getUserBy_id(s.getMuleID()).getUsername())
                    .usernameAdmin(userService.getUserBy_id(s.getAdminID()).getUsername())
                    .status(s.getStatus())
                    .startShiftId(s.getStartID().toString())
                    .stopShiftId(s.getStopID().toString())
                    .build();
            listShifts.add(shiftCreateVM);
        }
        return listShifts;
    }

    /**
     * controllo per evitare doppio turno nello stesso giorno nella stessa direzione per un mule
     * @param username mule scelto
     * @param data data in cui si controlla il doppio turno
     * @param direction direzione per il turno
     * @return
     */
    @Override
    public boolean controlDoubleTurn(String username, long data, boolean direction) {
        Shift s = this.getTurnByMuleDateDirection(username,data,direction);
        if (s == null)
            return false;
        else{
            if (s.getStatus().equals("rejected"))
                return false;
        }
        return true;
    }

    /**
     * prende tutti i turni per una linea da una settimana fa
     * @param routeID id della linea
     * @return ritorna tutti gli shift creati
     */
    @Override
    public List<ShiftCreateVM> getAllTurnsDate(int routeID) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<Shift> shifts = shiftRepo.findByLineaIDAndDateAfter(routeID, cal.getTimeInMillis());

        ArrayList<ShiftCreateVM> listShifts = new ArrayList<>();
        for (Shift s : shifts) {
            ShiftCreateVM shiftCreateVM = ShiftCreateVM.builder()
                    .shiftId(s.getTurnID().toString())
                    .data(s.getDate())
                    .direction(s.isDirection())
                    .lineId(s.getLineaID())
                    .username(userService.getUserBy_id(s.getMuleID()).getUsername())
                    .usernameAdmin(userService.getUserBy_id(s.getAdminID()).getUsername())
                    .status(s.getStatus())
                    .startShiftId(s.getStartID().toString())
                    .stopShiftId(s.getStopID().toString())
                    .build();
            listShifts.add(shiftCreateVM);
        }


        return listShifts;
    }

    /**
     *
     * @return il numero di prenotazioni attive oggi per i mule
     */
    @Override
    public int findNumberShiftToday() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(timeZone);
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);

        List<Shift> reservationList = shiftRepo.findByDate(today.getTimeInMillis());

        if(reservationList != null)
            return reservationList.size();
        else
            return 0;
    }
}
