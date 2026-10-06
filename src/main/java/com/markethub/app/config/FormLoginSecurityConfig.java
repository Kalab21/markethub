package com.markethub.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class FormLoginSecurityConfig {

    private final UserDetailsService userDetailsService;

    public FormLoginSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
                ))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers("/resources/static/**", "/images/**",
                        "/css/**", "/js/**", "/webfonts/**",
                        "/onlinemarket/public/**", "/onlinemarket/public/signup",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                .requestMatchers("/", "/onlinemarket").permitAll()
                // Role gates for endpoints outside the role-named view paths. Ownership of individual
                // records is checked in the controllers (see AccessGuard).
                .requestMatchers("/onlinemarket/secured/services/users/**").hasRole("ADMIN")
                .requestMatchers("/onlinemarket/secured/services/products/new-product",
                        "/onlinemarket/secured/services/products/update-product/**",
                        "/onlinemarket/secured/services/products/save-product",
                        "/onlinemarket/secured/services/products/*/delete",
                        "/onlinemarket/secured/services/products/my-products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers("/orders/**", "/onlinemarket/cart/**", "/api/cart/**").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("BUYER")
                .requestMatchers("/addresses/**", "/payment/**", "/reviews/**").hasRole("ADMIN")
                .requestMatchers("/onlinemarket/secured/services/admin/**").hasRole("ADMIN")
                .requestMatchers("/onlinemarket/secured/services/seller/**").hasRole("SELLER")
                .requestMatchers("/onlinemarket/secured/services/buyer/**").hasRole("BUYER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/onlinemarket/public/login")
                .defaultSuccessUrl("/onlinemarket/secured/services")
                .failureUrl("/onlinemarket/public/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/onlinemarket/public/logout", "POST"))
                .logoutSuccessUrl("/onlinemarket/public/login?logout")
                .permitAll()
            );
        return http.build();
    }
}
