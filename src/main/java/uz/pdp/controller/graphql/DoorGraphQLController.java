package uz.pdp.controller.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import uz.pdp.entity.Door;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.DoorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * GraphQL controller for door-related operations.
 * Provides endpoints for managing doors, including creation, updates, and queries.
 * Implements role-based access control for door management.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Controller
public class DoorGraphQLController {
    private static final Logger logger = LoggerFactory.getLogger(DoorGraphQLController.class);
    
    private final DoorService doorService;

    public DoorGraphQLController(DoorService doorService) {
        this.doorService = doorService;
    }

    /**
     * GraphQL query to retrieve all doors.
     * Requires admin privileges.
     *
     * @return List of all doors in the system
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Door> getAllDoors() {
        try {
            logger.info("GraphQL query: Retrieving all doors");
            List<Door> doors = doorService.getAllDoors();
            logger.info("Retrieved {} doors", doors.size());
            return doors;
        } catch (Exception e) {
            logger.error("Error retrieving doors via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL query to retrieve a door by ID.
     *
     * @param id ID of the door to retrieve
     * @return Door details if found
     */
    @QueryMapping
    public Door getDoorById(@Argument Long id) {
        try {
            logger.info("GraphQL query: Retrieving door with ID: {}", id);
            Door door = doorService.getDoorById(id);
            logger.info("Retrieved door: {}", door.getName());
            return door;
        } catch (Exception e) {
            logger.error("Error retrieving door {} via GraphQL: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL query to search for doors by criteria.
     *
     * @param searchTerm Search term to match against door properties
     * @return List of matching doors
     */
    @QueryMapping
    public List<Door> searchDoors(@Argument String searchTerm) {
        try {
            logger.info("GraphQL query: Searching doors with term: {}", searchTerm);
            List<Door> doors = doorService.searchDoors(searchTerm);
            logger.info("Found {} matching doors", doors.size());
            return doors;
        } catch (Exception e) {
            logger.error("Error searching doors via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to create a new door.
     * Requires seller or admin privileges.
     *
     * @param door Door details to create
     * @return Created door information
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Door createDoor(@Argument Door door) {
        try {
            logger.info("GraphQL mutation: Creating new door");
            EntityResponse<Door> response = doorService.createDoor(door);
            logger.info("Created door: {}", response.getData().getName());
            return response.getData();
        } catch (Exception e) {
            logger.error("Error creating door via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to update an existing door.
     * Requires appropriate permissions.
     *
     * @param id ID of the door to update
     * @param door Updated door details
     * @return Updated door information
     */
    @MutationMapping
    public Door updateDoor(@Argument Long id, @Argument Door door) {
        try {
            logger.info("GraphQL mutation: Updating door with ID: {}", id);
            EntityResponse<Door> response = doorService.updateDoor(id, door);
            logger.info("Updated door: {}", response.getData().getName());
            return response.getData();
        } catch (Exception e) {
            logger.error("Error updating door {} via GraphQL: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to delete a door.
     * Requires admin privileges.
     *
     * @param id ID of the door to delete
     * @return true if deletion successful
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteDoor(@Argument Long id) {
        try {
            logger.info("GraphQL mutation: Deleting door with ID: {}", id);
            EntityResponse<Void> response = doorService.deleteDoor(id);
            logger.info("Door {} deleted successfully", id);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error deleting door {} via GraphQL: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to configure door dimensions.
     * Requires seller or admin privileges.
     *
     * @param doorId ID of the door to configure
     * @param width New width for the door
     * @param height New height for the door
     * @return Updated door information
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Door configureDoorDimensions(
            @Argument Long doorId,
            @Argument Double width,
            @Argument Double height) {
        try {
            logger.info("GraphQL mutation: Configuring dimensions for door ID: {}", doorId);
            Door door = doorService.configureDoorDimensions(doorId, width, height);
            logger.info("Configured dimensions for door: {}", door.getName());
            return door;
        } catch (Exception e) {
            logger.error("Error configuring door dimensions via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to update door status.
     * Updates availability and active status.
     *
     * @param doorId ID of the door to update
     * @param isAvailable New availability status
     * @param isActive New active status
     * @return Updated door information
     */
    @MutationMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Door updateDoorStatus(
            @Argument Long doorId,
            @Argument Boolean isAvailable,
            @Argument Boolean isActive) {
        try {
            logger.info("GraphQL mutation: Updating status for door ID: {}", doorId);
            Door door = doorService.updateDoorStatus(doorId, isAvailable, isActive);
            logger.info("Updated status for door: {}", door.getName());
            return door;
        } catch (Exception e) {
            logger.error("Error updating door status via GraphQL: {}", e.getMessage());
            throw e;
        }
    }
}