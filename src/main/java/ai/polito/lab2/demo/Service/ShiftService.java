package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.Shift;
import org.bson.types.ObjectId;

public interface ShiftService {
    Shift save(ShiftDTO t);
    void delete (ObjectId turnID);
    Shift getTurnByID(ObjectId turnID);

    void editTurn(Shift t);
}
