package uz.pdp.controller.graphql;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import uz.pdp.entity.Door;
import uz.pdp.service.DoorService;
import graphql.GraphQLException;
import jakarta.transaction.Transactional;

import java.util.List;

@Controller
public class DoorGraphQLController {
    
    private static final Logger logger = LoggerFactory.getLogger(DoorGraphQLController.class);
    
    @Autowired
    private DoorService doorService;

    @QueryMapping
    @Transactional
    public Door door(@Argument Long id) {
        try {
            logger.info("GraphQL query: Fetching door by ID: {}", id);
            Door door = doorService.getDoor(id);
            Hibernate.initialize(door.getImages());
            return door;
        } catch (Exception e) {
            logger.error("Error fetching door with ID {}: {}", id, e.getMessage());
            throw new GraphQLException("Error fetching door: " + e.getMessage());
        }
    }

    @QueryMapping
    @Transactional
    public List<Door> doors() {
        try {
            logger.info("GraphQL query: Fetching all doors");
            List<Door> doors = doorService.getAllDoors();
            doors.forEach(door -> Hibernate.initialize(door.getImages()));
            return doors;
        } catch (Exception e) {
            logger.error("Error fetching all doors: {}", e.getMessage());
            throw new GraphQLException("Error fetching doors: " + e.getMessage());
        }
    }
}