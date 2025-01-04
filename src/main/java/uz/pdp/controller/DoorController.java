package uz.pdp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.service.DoorService;

import java.util.List;

@RestController
@RequestMapping("/api/doors")
@Tag(name = "Door Management", description = "APIs for managing doors")
public class DoorController {

    @Autowired
    private DoorService doorService;

    @GetMapping("/{id}")
    @Operation(summary = "Get door details by ID")
    public ResponseEntity<Door> getDoor(@PathVariable Long id) {
        return ResponseEntity.ok(doorService.getDoor(id));
    }

    @GetMapping
    @Operation(summary = "Get all doors")
    public ResponseEntity<List<Door>> getAllDoors() {
        return ResponseEntity.ok(doorService.getAllDoors());
    }

    @PostMapping
    @Operation(summary = "Create a new door")
    public ResponseEntity<Door> createDoor(@RequestBody Door door) {
        return ResponseEntity.ok(doorService.createDoor(door));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update door details")
    public ResponseEntity<Door> updateDoor(@PathVariable Long id, @RequestBody Door updatedDoor) {
        return ResponseEntity.ok(doorService.updateDoor(id, updatedDoor));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a door")
    public ResponseEntity<Void> deleteDoor(@PathVariable Long id) {
        doorService.deleteDoor(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/configure")
    @Operation(summary = "Configure door size and color")
    public ResponseEntity<Door> configureDoor(
        @PathVariable Long id,
        @RequestParam(required = false) Size size,
        @RequestParam(required = false) Color color,
        @RequestParam(required = false) Double width,
        @RequestParam(required = false) Double height
    ) {
        return ResponseEntity.ok(doorService.configureDoor(id, size, color, width, height));
    }

    @QueryMapping
    public Door doorById(@Argument Long id) {
        return doorService.getDoor(id);
    }

    @QueryMapping
    public Iterable<Door> doors() {
        return doorService.getAllDoors();
    }

    @MutationMapping
    public Door configureDoorGraphQL(@Argument DoorConfigInput input) {
        return doorService.configureDoor(
            input.getId(),
            input.getSize(),
            input.getColor(),
            input.getWidth(),
            input.getHeight()
        );
    }
}