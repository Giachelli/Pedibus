package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Route;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface RouteService {


        List<Route> getAllRoutes();  //getAllUsers

        Route getRoutesByName(String NameR);   //getUserById

        Route getRoutesByID(int routeID);   //getUserById

        void save(ArrayList<Route> r);

        RouteDTO findRouteByNameR(String nameR);

        int findIDRouteByNameR(String nameR);

        void saveRoute(RouteDTO r);

        void saveRoute(Route r);

       void readAll() throws IOException;

      /*  Route getReservationByDate(Route route);      //addNewUser

        Object addReservation(String userId);       //getAllUserSettings

        String updateReservation(String userId, String key);   //getUserSetting

        String deleteReservation(String userId, String key, String value);  //addUserSetting
*/
}
