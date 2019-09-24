package ai.polito.lab2.demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ReadRoute {


    public ArrayList<Route> readAll() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final File folder = new File("target/pedibus_routes");
        ArrayList<Route> routesArray = new ArrayList<>();

        for(final File file : Objects.requireNonNull(folder.listFiles()))
        {
            Route route = objectMapper.readValue(file, Route.class);
            route.setLastModified(file.lastModified());
            routesArray.add(route);
        }

        return routesArray;


    }
}