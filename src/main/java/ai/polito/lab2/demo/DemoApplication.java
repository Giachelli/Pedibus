package ai.polito.lab2.demo;

import ai.polito.lab2.demo.Repositories.RouteRepo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;


@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = {RouteRepo.class})
@EnableGlobalMethodSecurity(securedEnabled = true)
public class DemoApplication {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    public static void main(String[] args) throws IOException {
        SpringApplication.run(DemoApplication.class, args);
    }

    }

