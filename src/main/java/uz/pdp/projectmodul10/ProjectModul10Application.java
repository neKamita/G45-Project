package uz.pdp.projectmodul10;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.UserRepository;

@SpringBootApplication(scanBasePackages = {
    "uz.pdp",
    "uz.pdp.controller.graphql"
})
@EntityScan(basePackages = "uz.pdp.entity")
@EnableJpaRepositories(basePackages = "uz.pdp.repository")
public class ProjectModul10Application {


    

    public static void main(String[] args) {
        SpringApplication.run(ProjectModul10Application.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin already exists
            if (!userRepository.findByName("etadoor").isPresent()) {
                User admin = new User();
                admin.setName("etadoor");
                admin.setEmail("admin@etadoor.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setLastname("Admin");
                admin.setPhone("+1234567890");
                
                userRepository.save(admin);
            }
        };
    }
}