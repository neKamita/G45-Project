package uz.pdp.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import uz.pdp.config.filtr.MyFilter;

/**
     * Security configuration class
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@OpenAPIDefinition(info = @Info(title = "Etadoor API", version = "1.1.2"), servers = {
        @Server(url = "https://etadoor.up.railway.app", description = "Production Server"),
        @Server(url = "http://localhost:8080", description = "Local Development Server")
}, security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class MyConf {

    private final MyFilter myFilter;
    private final CustomUserDetailsService userDetailsService;

    public MyConf(MyFilter myFilter, CustomUserDetailsService userDetailsService) {
        this.myFilter = myFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints
                        .requestMatchers("/api/auth/**", "/api/users/verify-seller").permitAll()

                        // GraphQL endpoints
                        .requestMatchers("/graphql/**", "/graphiql/**").permitAll()
                        .requestMatchers("/subscriptions/**").permitAll()

                        .requestMatchers("/api/v1/additional/**").permitAll()


                        .requestMatchers(HttpMethod.GET,"/api/v1/doors/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/contacts/**").permitAll()

                        // Swagger/OpenAPI endpoints
                        .requestMatchers(HttpMethod.GET,
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        // Public API endpoints
                        .requestMatchers(HttpMethod.GET, "/api/doors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/contacts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/door-accessories/**").permitAll()

                        // Basket operations
                        .requestMatchers(HttpMethod.POST, "/api/doors/*/basket").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/accessories/*/basket").authenticated()

                        // Protected endpoints
                        .requestMatchers("/api/doors/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers("/api/contacts/**").hasRole("ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated())
                .addFilterBefore(myFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider())
                .httpBasic(Customizer.withDefaults());

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedOrigins(List.of(
                "http://localhost:8080",
                "https://etadoor.up.railway.app",
                "http://localhost:5173"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}