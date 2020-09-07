package ai.polito.lab2.demo.security.jwt;

import ai.polito.lab2.demo.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ai.polito.lab2.demo.security.jwt.JwtAuthenticationEntryPoint;

import java.util.Arrays;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;


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
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**");
    }


    @Override
    protected void configure (HttpSecurity http) throws Exception{
            http
                    //.addFilterBefore(corsFilter(), SessionManagementFilter.class) //adds your custom CorsFilter
                    .httpBasic().disable()
                    .csrf().disable()
                    .anonymous().and().cors().and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(unauthorizedHandler)
                    .and()
                    .authorizeRequests()
                    .antMatchers("/login").permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/recover").permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/api-docs").permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/register").hasRole("SYSTEM_ADMIN")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/user", "/user/**").hasAnyRole("SYSTEM_ADMIN","ADMIN","USER")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/confirm/*").permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/users", "/users/*").hasAnyRole("ADMIN","SYSTEM_ADMIN")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/routes").permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/routes/addRoute").hasRole("SYSTEM_ADMIN")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/reservations/**").hasRole("USER")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/shift/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN","MULE")
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .apply(new JwtConfigurer(jwtTokenProvider));

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}

