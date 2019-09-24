package ai.polito.lab2.demo.controllers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
public class AuthenticationRequest {
    private String username;
    private String password;
}