package com.example.securitydemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    DataSource dataSource;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests((request)->
                request.requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));   //disabling cookie
        //http.formLogin(withDefaults());
        http.httpBasic(withDefaults());
        http.headers(headers ->
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        http.csrf(csrf-> csrf.disable());
        return http.build();
    }

    //In memory authentication
    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails user1 = User.withUsername("user1")
//                .password("{noop}password1")  //noop tells spring that password should be saved as plain text
                .password(passwordEncoder().encode("password1"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
//                .password("{noop}adminPass")  //noop tells spring that password should be saved as plain text
                .password(passwordEncoder().encode("adminPass"))
                .roles("ADMIN")
                .build();

        JdbcUserDetailsManager jdbcUserDetailsManager
                =new JdbcUserDetailsManager(dataSource);

        jdbcUserDetailsManager.createUser(user1);
        jdbcUserDetailsManager.createUser(admin);

        return jdbcUserDetailsManager;
        //return new InMemoryUserDetailsManager(user1,admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
