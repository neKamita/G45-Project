package uz.pdp.projectmodul10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "uz.pdp",
    "uz.pdp.controller.graphQL"
})
@EntityScan(basePackages = "uz.pdp.entity")
@EnableJpaRepositories(basePackages = "uz.pdp.repository")
public class ProjectModul10Application {
    public static void main(String[] args) {
        SpringApplication.run(ProjectModul10Application.class, args);
    }
}
