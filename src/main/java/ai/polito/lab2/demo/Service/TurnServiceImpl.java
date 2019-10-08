package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Turn;
import ai.polito.lab2.demo.Repositories.TurnRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TurnServiceImpl implements TurnService {

    @Autowired
    private TurnRepo turnRepo;


    @Override
    public void save(Turn t) {
        turnRepo.save(t);
    }

    @Override
    public Turn getTurnByID(ObjectId turnID) {
        return turnRepo.getTurnByTurnID(turnID);
    }
}
