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
    public ResponseEntity<Door> getDoor(@PathVariable Long id) {
        logger.info("Fetching door with ID: {}", id);
        Door door = doorService.getDoor(id);
        logger.info("Retrieved door: {}", door);
        return ResponseEntity.ok(door);
    }

    @GetMapping
    @Operation(summary = "Get all doors")
    public ResponseEntity<List<Door>> getAllDoors() {
        logger.info("Fetching all doors");
        List<Door> doors = doorService.getAllDoors();
        logger.info("Retrieved {} doors", doors.size());
        return ResponseEntity.ok(doors);
    }

    @PostMapping
    @Operation(summary = "Create a new door")
    public ResponseEntity<Door> createDoor(@RequestBody Door door) {
        logger.info("Creating a new door: {}", door);
        Door createdDoor = doorService.createDoor(door);
        logger.info("Created door with ID: {}", createdDoor.getId());
        return ResponseEntity.ok(createdDoor);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update door details")
    public ResponseEntity<Door> updateDoor(@PathVariable Long id, @RequestBody Door updatedDoor) {
        logger.info("Updating door with ID: {}, new data: {}", id, updatedDoor);
        Door door = doorService.updateDoor(id, updatedDoor);
        logger.info("Updated door: {}", door);
        return ResponseEntity.ok(door);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a door")
    public ResponseEntity<Void> deleteDoor(@PathVariable Long id) {
        logger.info("Deleting door with ID: {}", id);
        doorService.deleteDoor(id);
        logger.info("Successfully deleted door with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/configure")
    @Operation(summary = "Configure door size and color")
    public ResponseEntity<Door> configureDoor(
        @PathVariable Long id,
        @RequestBody DoorConfigInput configInput
    ) {
        logger.info("Configuring door with ID: {}, configuration: {}", id, configInput);
        Door configuredDoor = doorService.configureDoor(
            id,
            configInput.getSize(),
            configInput.getColor(),
            configInput.getWidth(),
            configInput.getHeight()
        );
        logger.info("Configured door: {}", configuredDoor);
        return ResponseEntity.ok(configuredDoor);
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