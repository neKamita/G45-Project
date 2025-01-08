package uz.pdp.controller.graphQL;

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

@Controller
public class DoorGraphQLController {
    
    private static final Logger logger = LoggerFactory.getLogger(DoorGraphQLController.class);
    
    @Autowired
    private DoorService doorService;

    @QueryMapping(name = "door")
    public Door doorById(@Argument("id") Long id) {
        logger.info("GraphQL query: Fetching door by ID: {}", id);
        return doorService.getDoor(id);
    }

    @QueryMapping(name = "doors")
    public Iterable<Door> getAllDoors() {
        logger.info("GraphQL query: Fetching all doors");
        return doorService.getAllDoors();
    }

    @MutationMapping(name = "configureDoor")
    public Door configureDoor(@Argument("input") DoorConfigInput input) {
        logger.info("GraphQL mutation: Configuring door with ID: {}, input: {}", input.getId(), input);
        return doorService.configureDoor(
            input.getId(),
            input.getSize(),
            input.getColor(),
            input.getWidth(),
            input.getHeight()
        );
    }
}