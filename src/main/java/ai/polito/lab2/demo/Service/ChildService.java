package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ChildDTO;
import ai.polito.lab2.demo.Entity.Child;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface ChildService {
    ChildDTO findChildDTObyID(ObjectId childId);

    Child findChildbyID(ObjectId childId);

    ArrayList<ChildDTO> findChildbyUsername(String username);

    ChildDTO findChildbyFamilyName(String familyName);

    void saveChild(ChildDTO childDTO, ObjectId familyID);

    Child findChildByNameChildAndUsername(String nameChild, String username);

    ArrayList<Child> findAllChild();
}
