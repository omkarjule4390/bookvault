package com.librarymanagement.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Only administrators may delete books or members (HTTP DELETE on /api/**).
 * Every other request stays public, as before.
 *
 * The admin account comes from the ADMIN_USERNAME and ADMIN_PASSWORD environment variables.
 * If either one is missing, no admin exists and all deletes are rejected (secure by default).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                // 401 without a WWW-Authenticate header, so the browser does not show its own login popup
                .httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${ADMIN_USERNAME:}") String username,
            @Value("${ADMIN_PASSWORD:}") String password,
            @Value("${ADMIN_USERS:}") String moreAdmins,
            PasswordEncoder encoder) {
        List<UserDetails> admins = new ArrayList<>();
        if (!username.isBlank() && !password.isBlank()) {
            admins.add(User.withUsername(username).password(encoder.encode(password)).roles("ADMIN").build());
        }
        // Optional extra admins: ADMIN_USERS="name1:password1,name2:password2"
        for (String entry : moreAdmins.split(",")) {
            int colon = entry.indexOf(':');
            if (colon > 0 && colon < entry.length() - 1) {
                String name = entry.substring(0, colon).trim();
                String pass = entry.substring(colon + 1).trim();
                if (!name.isEmpty() && !pass.isEmpty() && !name.equals(username)) {
                    admins.add(User.withUsername(name).password(encoder.encode(pass)).roles("ADMIN").build());
                }
            }
        }
        return new InMemoryUserDetailsManager(admins);
    }
}
