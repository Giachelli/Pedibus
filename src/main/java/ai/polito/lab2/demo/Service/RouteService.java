package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface RouteService {


        List<Route> getAllRoutes();  //getAllUsers

        Route getRoutesByName(String NameR);   //getUserById

        ArrayList<Route> PopulateDb () throws IOException;

        void save(ArrayList<Route> r);

      /*  Route getReservationByDate(Route route);      //addNewUser

        Object addReservation(String userId);       //getAllUserSettings

        String updateReservation(String userId, String key);   //getUserSetting

        String deleteReservation(String userId, String key, String value);  //addUserSetting
*/
}
