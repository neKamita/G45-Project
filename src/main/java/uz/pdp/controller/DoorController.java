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