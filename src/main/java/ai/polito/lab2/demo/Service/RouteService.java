package ai.polito.lab2.demo.Service;

import ai.polito.lab2.demo.Dto.RouteDTO;
import ai.polito.lab2.demo.Entity.Route;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface RouteService {


        List<Route> getAllRoutes();  //getAllUsers

        Route getRoutesByName(String NameR);   //getUserById

        Route getRoutesByID(int routeID);   //getUserById

        RouteDTO getRoutesDTOByID(int routeID);   //getUserById

        void saveAll(ArrayList<Route> r);

        RouteDTO findRouteByNameR(String nameR);

        int findIDRouteByNameR(String nameR);

        void saveRoute(RouteDTO r);

        void saveRoute(Route r);

       void readAll() throws IOException;

    Route readSingle(File file) throws IOException;

    List<String> getAccompagnaotori(int lineaID);

      /*  Route getReservationByDate(Route route);      //addNewUser

        Object addReservation(String userId);       //getAllUserSettings

        String updateReservation(String userId, String key);   //getUserSetting

        String deleteReservation(String userId, String key, String value);  //addUserSetting
*/
}
