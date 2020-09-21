package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Entity.Stop;
import ai.polito.lab2.demo.Repositories.StopRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StopServiceImpl implements StopService{


    @Autowired
    private StopRepo stopRepo;


    public Stop findStopbyId(ObjectId id){
        return stopRepo.findStopBy_id(id);
    }

    /**
     * Ritorna lo stop data il nome della fermata e il numero di sequenza
     * @param name
     * @param nums
     * @return
     */
    @Override
    public Stop findStopbyNameAndNumS(String name, int nums) {
        return stopRepo.findStopByNomeAndNums(name,nums);
    }

}
