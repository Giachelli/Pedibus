package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ShiftDTO;
import ai.polito.lab2.demo.Entity.Shift;
import ai.polito.lab2.demo.viewmodels.ShiftCreateVM;
import org.bson.types.ObjectId;

import java.util.List;

public interface ShiftService {
    Shift save(ShiftDTO t);
    void delete (ObjectId turnID);
    Shift getTurnByID(ObjectId turnID);

    void editTurn(Shift t);

    List<ShiftCreateVM> getTurns(int routeID, ObjectId muleID);

    Shift getTurnByMuleDateDirection(String mule, long date, boolean dir);

    List<ShiftCreateVM> getTurnsRoute(int routeID);

    List<ShiftCreateVM> getTurnsDate(int routeID, ObjectId muleID);

    boolean controlDoubleTurn(String username, long data, boolean direction);

    List<Shift> getTurnsByLineaIDMuleIDDateDirection(int routeID, ObjectId muleID, long date, String dir);

    List<ShiftCreateVM> getAllTurnsDate(int routeID);

    int findNumberShiftToday();
}
