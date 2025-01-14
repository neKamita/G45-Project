package uz.pdp.controller.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;

import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.service.DoorService;
import graphql.GraphQLException;



import java.util.List;

@Controller
public class DoorGraphQLController {
    private final DoorService doorService;
    private static final Logger logger = LoggerFactory.getLogger(DoorGraphQLController.class);

    @Autowired
    public DoorGraphQLController(DoorService doorService) {
        this.doorService = doorService;
    }

    @QueryMapping
    @Transactional()
    public Door door(@Argument Long id) {
        try {
            logger.info("GraphQL Query: Fetching door {}", id);
            return doorService.getDoor(Long.valueOf(id.toString()));
        } catch (Exception e) {
            logger.error("Error fetching door: {}", e.getMessage());
            throw new GraphQLException("Error fetching door: " + e.getMessage());
        }
    }

    @QueryMapping
    public List<Door> doors() {
        try {
            logger.info("GraphQL Query: Fetching all doors");
            return doorService.getAllDoors();
        } catch (Exception e) {
            logger.error("Error fetching doors: {}", e.getMessage());
            throw new GraphQLException("Error fetching doors: " + e.getMessage());
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('SELLER')")
    public Door createDoor(@Argument("input") DoorConfigInput input) {
        try {
            logger.info("GraphQL Mutation: Creating door");
            return doorService.createDoor(input.toDoorDTO());
        } catch (Exception e) {
            logger.error("Error creating door: {}", e.getMessage());
            throw new GraphQLException("Error creating door: " + e.getMessage());
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN') or @doorSecurityService.isSeller(#input.id)")
    public Door configureDoor(@Argument("input") DoorConfigInput input) {
        try {
            logger.info("GraphQL Mutation: Configuring door {}", input.getId());
            return doorService.configureDoor(
                input.getId(),
                input.getSize(),
                input.getColor(),
                input.getWidth(),
                input.getHeight()
            );
        } catch (Exception e) {
            logger.error("Error configuring door: {}", e.getMessage());
            throw new GraphQLException("Error configuring door: " + e.getMessage());
        }
    }
}