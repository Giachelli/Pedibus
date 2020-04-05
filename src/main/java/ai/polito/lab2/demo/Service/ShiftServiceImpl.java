package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.Shift;
import ai.polito.lab2.demo.Repositories.ShiftRepo;
import ai.polito.lab2.demo.viewmodels.ShiftCreateVM;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private ShiftRepo shiftRepo;

    @Autowired
    private UserService userService;


    @Override
    public Shift save(ShiftDTO t) {
       Shift shift = Shift.builder().muleID(t.getMuleID())
                    .AdminID(t.getAdminID()).date(t.getData())
                    .lineaID(t.getLineId()).direction(t.isDirection()).build();

        return shiftRepo.save(shift);
    }

    @Override
    public void delete(ObjectId turnID) {

        shiftRepo.delete(this.getTurnByID(turnID));

    }

    @Override
    public Shift getTurnByID(ObjectId turnID) {
        return shiftRepo.getTurnByTurnID(turnID);
    }

    @Override
    public void editTurn(Shift t) {
        shiftRepo.save(t);
    }

    @Override
    public List<ShiftCreateVM> getTurns(int routeID, ObjectId muleID) {
      List<Shift> shifts =  shiftRepo.findByLineaIDAndMuleID(routeID, muleID);

        ArrayList<ShiftCreateVM> listShifts = new ArrayList<>();
      for(Shift s : shifts)
      {
          ShiftCreateVM shiftCreateVM = ShiftCreateVM.builder()
                  .shiftId(s.getTurnID().toString())
                  .data(s.getDate())
                  .direction(s.isDirection())
                  .lineId(s.getLineaID())
                  .username(userService.getUserBy_id(s.getMuleID()).getUsername())
                  .usernameAdmin(userService.getUserBy_id(s.getAdminID()).getUsername())
                  .build();
          listShifts.add(shiftCreateVM);
      }
      return listShifts;
    }

    @Override
    public List<ShiftCreateVM> getTurnsDate(int routeID, ObjectId muleID) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<Shift> shifts =  shiftRepo.findByLineaIDAndMuleIDAndDateAfter(routeID, muleID,cal.getTimeInMillis());

        ArrayList<ShiftCreateVM> listShifts = new ArrayList<>();
        for(Shift s : shifts)
        {
            ShiftCreateVM shiftCreateVM = ShiftCreateVM.builder()
                    .shiftId(s.getTurnID().toString())
                    .data(s.getDate())
                    .direction(s.isDirection())
                    .lineId(s.getLineaID())
                    .username(userService.getUserBy_id(s.getMuleID()).getUsername())
                    .usernameAdmin(userService.getUserBy_id(s.getAdminID()).getUsername())
                    .build();
            listShifts.add(shiftCreateVM);
        }
        return listShifts;
    }
}
