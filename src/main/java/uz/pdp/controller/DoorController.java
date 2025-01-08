package uz.pdp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.service.DoorService;
import org.springframework.http.HttpStatus;
import uz.pdp.payload.EntityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/doors")
@Tag(name = "Door Management", description = "APIs for managing doors")
public class DoorController {

    private static final Logger logger = LoggerFactory.getLogger(DoorController.class);

    @Autowired
    private DoorService doorService;

    @GetMapping("/{id}")
    @Operation(summary = "Get door details by ID")
    public ResponseEntity<EntityResponse<Door>> getDoor(@PathVariable Long id) {
        logger.info("Fetching door with ID: {}", id);
        Door door = doorService.getDoor(id);
        logger.info("Retrieved door: {}", door);
        return ResponseEntity.ok(EntityResponse.success(door));
    }

    @GetMapping
    @Operation(summary = "Get all doors")
    public ResponseEntity<EntityResponse<List<Door>>> getAllDoors() {
        logger.info("Fetching all doors");
        List<Door> doors = doorService.getAllDoors();
        logger.info("Retrieved {} doors", doors.size());
        return ResponseEntity.ok(EntityResponse.success(doors));
    }

    @PostMapping
    @Operation(summary = "Create a new door")
    public ResponseEntity<EntityResponse<Door>> createDoor(@RequestBody Door door) {
        logger.info("Creating a new door: {}", door);
        Door createdDoor = doorService.createDoor(door);
        logger.info("Created door with ID: {}", createdDoor.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(EntityResponse.created(createdDoor));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update door details")
    public ResponseEntity<EntityResponse<Door>> updateDoor(
            @PathVariable Long id, 
            @RequestBody Door updatedDoor) {
        logger.info("Updating door with ID: {}, new data: {}", id, updatedDoor);
        Door door = doorService.updateDoor(id, updatedDoor);
        logger.info("Updated door: {}", door);
        return ResponseEntity.ok(EntityResponse.success("Door updated successfully", door));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a door")
    public ResponseEntity<EntityResponse<Void>> deleteDoor(@PathVariable Long id) {
        logger.info("Deleting door with ID: {}", id);
        doorService.deleteDoor(id);
        logger.info("Successfully deleted door with ID: {}", id);
        return ResponseEntity.ok(EntityResponse.deleted());
    }

    @PostMapping("/{id}/configure")
    @Operation(summary = "Configure door size and color")
    public ResponseEntity<EntityResponse<Door>> configureDoor(
            @PathVariable Long id,
            @RequestBody DoorConfigInput configInput) {
        logger.info("Configuring door with ID: {}, configuration: {}", id, configInput);
        Door configuredDoor = doorService.configureDoor(
            id,
            configInput.getSize(),
            configInput.getColor(),
            configInput.getWidth(),
            configInput.getHeight()
        );
        logger.info("Configured door: {}", configuredDoor);
        return ResponseEntity.ok(EntityResponse.success("Door configured successfully", configuredDoor));
    }

    @QueryMapping
    public Door doorById(@Argument Long id) {
        logger.info("GraphQL query: Fetching door by ID: {}", id);
        return doorService.getDoor(id);
    }

    @QueryMapping
    public Iterable<Door> doors() {
        logger.info("GraphQL query: Fetching all doors");
        return doorService.getAllDoors();
    }

    @MutationMapping
    public Door configureDoorGraphQL(@Argument DoorConfigInput input) {
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