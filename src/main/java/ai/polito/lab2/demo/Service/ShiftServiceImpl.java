package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.Shift;
import ai.polito.lab2.demo.Repositories.ShiftRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private ShiftRepo shiftRepo;


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
}
