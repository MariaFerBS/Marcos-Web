
package com.marcos.proyecto.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.marcos.proyecto.repository.UsuarioRepository;
import com.marcos.proyecto.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    // Encriptación de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Autenticación de usuarios
    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // Guardar información del usuario al iniciar sesión
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler(
            UsuarioRepository usuarioRepository) {

        return (request, response, authentication) -> {

            usuarioRepository
                .findByEmailUsIgnoreCase(authentication.getName())
                .ifPresent(usuario -> {

                    request.getSession().setAttribute(
                            "usuarioLogueado", usuario.getIdUs());

                    request.getSession().setAttribute(
                            "nombreUsuario", usuario.getNomUs());

                    request.getSession().setAttribute(
                            "rolUsuario", usuario.getRolUs());
                });

            response.sendRedirect("/dashboard");
        };
    }

    // Configuración CORS para permitir la conexión desde Live Server
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
            "http://localhost:5500",
            "http://127.0.0.1:5500"
        ));

        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // Configuración principal de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            AuthenticationSuccessHandler loginSuccessHandler)
            throws Exception {

        http
            // Activar CORS
            .cors(Customizer.withDefaults())

            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                // Rutas públicas
                .requestMatchers(
                    "/", "/login", "/error",
                    "/images/**", "/css/**", "/js/**"
                ).permitAll()

                // API de consulta de productos para ambos roles
                .requestMatchers(
                    HttpMethod.GET, "/productos/dto"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                // Gestión administrativa solo ADMIN
                .requestMatchers(
                    "/usuarios", "/usuarios/**",
                    "/categorias", "/categorias/**",
                    "/proveedores", "/proveedores/**",
                    "/movimientos", "/movimientos/**",
                    "/graficos", "/graficos/**"
                ).hasRole("ADMIN")

                // Consulta de productos para ADMIN y VENDEDOR
                .requestMatchers(
                    HttpMethod.GET, "/productos", "/productos/"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                // Otras operaciones de productos solo ADMIN
                .requestMatchers("/productos/**")
                .hasRole("ADMIN")

                // Dashboard y ventas para ambos roles
                .requestMatchers(
                    "/dashboard/**",
                    "/ventas/**"
                ).hasAnyRole("ADMIN", "VENDEDOR")

                // Las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )

            // Inicio de sesión
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Cierre de sesión
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
