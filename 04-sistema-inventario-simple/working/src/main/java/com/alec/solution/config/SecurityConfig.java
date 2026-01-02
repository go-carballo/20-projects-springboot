package com.alec.solution.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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

/**
 * Configuracion de seguridad para la API de inventario.
 * 
 * Roles:
 * - VIEWER: Solo puede consultar productos (GET)
 * - OPERATOR: Puede consultar y gestionar stock (GET, POST stock)
 * - ADMIN: Acceso total (CRUD completo)
 * 
 * Usuarios por defecto:
 * - viewer / viewer123 (VIEWER)
 * - operator / operator123 (OPERATOR)
 * - admin / admin123 (ADMIN)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger UI y OpenAPI - acceso publico
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Actuator health - acceso publico
                .requestMatchers("/actuator/health").permitAll()
                
                // GET requests - VIEWER, OPERATOR, ADMIN
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("VIEWER", "OPERATOR", "ADMIN")
                
                // Stock operations - OPERATOR, ADMIN
                .requestMatchers(HttpMethod.POST, "/api/products/*/stock/**").hasAnyRole("OPERATOR", "ADMIN")
                
                // Reactivar - ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/products/*/reactivar").hasRole("ADMIN")
                
                // Create, Update, Delete - ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails viewer = User.builder()
                .username("viewer")
                .password(passwordEncoder.encode("viewer123"))
                .roles("VIEWER")
                .build();

        UserDetails operator = User.builder()
                .username("operator")
                .password(passwordEncoder.encode("operator123"))
                .roles("OPERATOR")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(viewer, operator, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
