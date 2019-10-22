package ai.polito.lab2.demo.security.jwt;

import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpResponse;

public class InvalidJwtAuthenticationException extends AuthenticationException {
    public InvalidJwtAuthenticationException(String e) {

        super(e);
        HttpServletResponse httpServletResponse = null;
        httpServletResponse.setStatus(401);
    }
}
