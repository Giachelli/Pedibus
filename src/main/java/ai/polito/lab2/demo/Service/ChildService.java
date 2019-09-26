package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ChildDTO;
import ai.polito.lab2.demo.Entity.Child;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public interface ChildService {
    ChildDTO findChildbyID(ObjectId childId);

    ArrayList<ChildDTO> findChildbyFamilyID(ObjectId familyId);

    ChildDTO findChildbyFamilyName(String familyName);

    void saveChild(ChildDTO childDTO, ObjectId familyID);
}
