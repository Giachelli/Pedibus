package ai.polito.lab2.demo.security.jwt;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@ControllerAdvice

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

        final String expired = (String) httpServletRequest.getAttribute("expired");
        System.out.println(expired);
        if (expired!=null){
            logger.error("Responding with unauthorized error. Message - {}", expired);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,expired);
            System.out.println("HEI CI SONO");


        }else{
            logger.error("Responding with unauthorized error. Message - {}", e.getMessage());
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,e.getMessage());
        }
    }

}
