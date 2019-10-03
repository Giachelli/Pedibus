package ai.polito.lab2.demo.security.jwt;

import ai.polito.lab2.demo.security.CustomUserDetailsService;
import ai.polito.lab2.demo.security.jwt.JwtTokenProvider;
import ai.polito.lab2.demo.security.jwt.JwtConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
       super.configure(auth);

    }


    @Override
    protected void configure (HttpSecurity http) throws Exception{
            http
                    .httpBasic().disable()
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and().authorizeRequests()
                    .antMatchers("/login").permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/register").hasRole("SYSTEM_ADMIN")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/confirm/*").permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/users", "/users/*").hasAnyRole("ADMIN","SYSTEM_ADMIN")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/lines", "/lines/*").hasAnyRole("ADMIN","SYSTEM_ADMIN")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/reservations/**").hasRole("USER")
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .apply(new JwtConfigurer(jwtTokenProvider));

    }

}

