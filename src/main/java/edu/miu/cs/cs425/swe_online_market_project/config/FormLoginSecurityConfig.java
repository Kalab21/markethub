package edu.miu.cs.cs425.swe_online_market_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FormLoginSecurityConfig {

    private UserDetailsService userDetailsService;

    public FormLoginSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions().sameOrigin()
                .and()
                .authorizeRequests()
                .antMatchers("/resources/static/**", "/images/**", "/css/**", "/onlinemarket/public/**", "/onlinemarket/public/signup",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/", "/onlinemarket").permitAll()
                .antMatchers("/onlinemarket/secured/services/admin/**").hasRole("ADMIN")
                .antMatchers("/onlinemarket/secured/services/seller/**").hasRole("SELLER")
                .antMatchers("/onlinemarket/secured/services/buyer/**").hasRole("BUYER")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/onlinemarket/public/login")
                .defaultSuccessUrl("/onlinemarket/secured/services")
                .failureUrl("/onlinemarket/public/login?error")
                .permitAll()
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/onlinemarket/public/logout"))
                .logoutSuccessUrl("/onlinemarket/public/login?logout")
                .permitAll()
                .and()
                .exceptionHandling();
           return http.build();
    }
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(this.userDetailsService);
                //.passwordEncoder(passwordEncoder());
    }

}
