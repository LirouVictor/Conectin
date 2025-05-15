package com.conectin.conectin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // IMPORTANTE: Adicionar este import
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// Não precisamos de CorsConfiguration e List aqui se a configuração do WebConfig for suficiente
// import org.springframework.web.cors.CorsConfiguration;
// import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // A configuração de CORS via WebConfig.addCorsMappings é geralmente preferida.
            // Se você precisar de algo mais específico do Spring Security, pode habilitar o .cors() abaixo.
            // .cors(cors -> cors.configurationSource(request -> {
            //     CorsConfiguration config = new CorsConfiguration();
            //     config.setAllowedOrigins(List.of("http://localhost:8081"));
            //     config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            //     config.setAllowedHeaders(List.of("*"));
            //     config.setAllowCredentials(true);
            //     return config;
            // }))
            .csrf(csrf -> csrf.disable()) // CSRF desativado
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // Permite chamadas para APIs sem autenticação
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll() // <<< --- ADICIONE ESTA LINHA
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // O CorsFilter bean pode ser redundante se WebConfig.addCorsMappings estiver funcionando.
    // Mantenha comentado a menos que você ative o .cors() acima.
    // @Bean
    // public CorsFilter corsFilter() {
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowedOrigins(List.of("http://localhost:8081"));
    //     config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    //     config.setAllowedHeaders(List.of("*"));
    //     config.setAllowCredentials(true);
    //     source.registerCorsConfiguration("/**", config);
    //     return new CorsFilter(source);
    // }
}