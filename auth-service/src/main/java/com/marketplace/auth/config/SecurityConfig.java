package com.marketplace.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
// Активирует цепочку фильтров безопасности для веб-приложения
@EnableWebSecurity
public class SecurityConfig {
    /**
     * UserDetailsService — его задача — по имени пользователя (логину) найти данные этого пользователя
     * (пароль, роли, флаги блокировки). Spring Security использует его во время логина:
     * когда пользователь вводит логин/пароль, Security вызывает loadUserByUsername(login), получает объект UserDetails,
     * и сравнивает введённый пароль с тем, что внутри.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.withUsername("user")
                .password(encoder.encode("pass"))
                .roles("USER")
                .build();

        UserDetails seller = User.withUsername("seller")
                .password(encoder.encode("pass"))
                .roles("SELLER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("pass"))
                .roles("ADMIN")
                .build();
        // храним пользователей в памяти
        return new InMemoryUserDetailsManager(user, seller, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()   // логин/регистрация/рефреш — открыты
                        .requestMatchers("/actuator/**").permitAll() // healthcheck — открыт
                        .anyRequest().authenticated()              // остальное — только с токеном
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }
}