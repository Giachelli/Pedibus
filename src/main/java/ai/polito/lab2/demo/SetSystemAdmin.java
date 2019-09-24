/*
package ai.polito.lab2.demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SetSystemAdmin {


    public static User readAdminSystem() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final File folder = new File("target/System_admin");
        User user = new User();
        for(final File file : Objects.requireNonNull(folder.listFiles()))
        {
            user = objectMapper.readValue(file, User.class);

        }

        return user;

    }
}*/
