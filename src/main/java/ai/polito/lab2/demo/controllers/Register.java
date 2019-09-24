package ai.polito.lab2.demo.controllers;

import lombok.Data;

@Data
public class Register
{
    //private int _id;
    private String username;
    private String password;
    private String confirmPassword;
}
