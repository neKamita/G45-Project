package uz.pdp.projectmodul10;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import graphql.schema.GraphQLScalarType;
import graphql.scalars.ExtendedScalars;
import graphql.schema.Coercing;

@SpringBootApplication
@ComponentScan(basePackages = {
    "uz.pdp.controller",
    "uz.pdp.controller.graphql",
    "uz.pdp.service",
    "uz.pdp.config",
    "uz.pdp.exception",
    "uz.pdp.mapper",
    "uz.pdp"
})
@EntityScan(basePackages = "uz.pdp.entity")
@EnableJpaRepositories(basePackages = "uz.pdp.repository")
public class ProjectModul10Application {

    @Bean
    GraphQlSourceBuilderCustomizer schemaCustomizer() {
        return builder -> builder
            .configureRuntimeWiring(wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.DateTime)
                .scalar(GraphQLScalarType.newScalar()
                    .name("JSON")
                    .description("JSON scalar type")
                    .coercing(new Coercing<Object, Object>() {
                        @Override
                        public Object serialize(Object input) { return input; }
                        @Override
                        public Object parseValue(Object input) { return input; }
                        @Override
                        public Object parseLiteral(Object input) { return input; }
                    })
                    .build()));
    }

    public static void main(String[] args) {
        System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
        SpringApplication.run(ProjectModul10Application.class, args);
    }
}
