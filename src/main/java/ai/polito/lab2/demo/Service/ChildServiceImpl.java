package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.ChildDTO;
import ai.polito.lab2.demo.Entity.Child;
import ai.polito.lab2.demo.Repositories.ChildRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ChildServiceImpl implements ChildService {

    @Autowired
    private ChildRepo childRepo;


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
    public ArrayList<ChildDTO> findChildbyUserID(ObjectId userId) {
        ArrayList<Child> childs = childRepo.findChildByUserID(userId);
        ArrayList<ChildDTO> childDTOS = new ArrayList<>();
        if (childs.isEmpty()) {
            ChildDTO c = ChildDTO.builder().build();
            childDTOS.add(c);
        } else {
            for (Child c : childs)
            {
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
}
