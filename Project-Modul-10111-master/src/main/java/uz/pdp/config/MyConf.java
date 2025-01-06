package uz.pdp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uz.pdp.config.filtr.MyFilter;
import uz.pdp.repository.UserRepository;


import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MyConf {
    final UserRepository userRepository;
    final MyFilter myFilter;




    


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(c -> corsConfigurationSource())
                .csrf(c ->c.disable())
                .authorizeHttpRequests(
                        auth->
                                auth
                                        .requestMatchers(new String[]{"/api/auth/**","swagger-ui/**","/v3/api-docs/**"})
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET,"/product/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE,"/product/**")
                                        .hasRole("ADMIN")
                                        .anyRequest()
                                        .authenticated()
                )
                .addFilterBefore(myFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }



    @Bean
    UserDetailsService userDetailsService(){
        return (username)->{
            return userRepository.findByUsername(username).orElseThrow();
        };
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"
//                "http://localhost:8080",
//                "http://localhost:5173"
        ));
        configuration.setAllowedHeaders(List.of("*"
                /*"Accept",
                "Content-Type",
                "Authorization"*/
        ));
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "DELETE", "PUT", "PATCH"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        /*source.registerCorsConfiguration("/api/v2/**", configuration2);
        source.registerCorsConfiguration("/api/v3/**", configuration3);*/
        return source;
    }
}
