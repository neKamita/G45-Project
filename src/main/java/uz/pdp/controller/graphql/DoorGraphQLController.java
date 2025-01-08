package uz.pdp.controller.graphql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import uz.pdp.entity.Door;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.service.DoorService;

import java.util.List;

@Controller
public class DoorGraphQLController {
    
    private static final Logger logger = LoggerFactory.getLogger(DoorGraphQLController.class);
    
    @Autowired
    private DoorService doorService;

    @QueryMapping
    public Door door(@Argument Long id) {
        try {
            logger.info("GraphQL query: Fetching door by ID: {}", id);
            return doorService.getDoor(id);
        } catch (Exception e) {
            logger.error("Error fetching door with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @QueryMapping
    public List<Door> doors() {
        try {
            logger.info("GraphQL query: Fetching all doors");
            return doorService.getAllDoors();
        } catch (Exception e) {
            logger.error("Error fetching all doors: {}", e.getMessage());
            throw e;
        }
    }
}