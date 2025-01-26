package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.entity.Moulding;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.MouldingService;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing moulding operations.
 * Handles CRUD operations for mouldings, with role-based access control.
 */
@RestController
@RequestMapping("/api/mouldings")
@Tag(name = "Moulding Management", description = "APIs for managing mouldings")
public class MouldingController {

    @Autowired
    private MouldingService mouldingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    @Operation(summary = "Get all mouldings", description = "Retrieves a list of all mouldings")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved mouldings")
    public List<Moulding> getAllMouldings() {
        return mouldingService.getAllMouldings();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    @Operation(summary = "Get a moulding by ID", description = "Retrieves a moulding by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the moulding"),
            @ApiResponse(responseCode = "404", description = "Moulding not found")
    })
    public EntityResponse<?> getMouldingById(@Parameter(description = "ID of the moulding") @PathVariable Long id) {
        Optional<Moulding> moulding = mouldingService.getMouldingById(id);
        if (moulding.isPresent()) {
            return EntityResponse.success("Moulding retrieved successfully", moulding.get());
        } else {
            return EntityResponse.error("Moulding not found", null);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Create a new moulding", description = "Creates a new moulding entry")
    @ApiResponse(responseCode = "200", description = "Moulding created successfully")
    public EntityResponse<Moulding> createMoulding(@RequestBody Moulding moulding) {
        Moulding createdMoulding = mouldingService.saveMoulding(moulding);
        return EntityResponse.success("Moulding created successfully", createdMoulding);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update a moulding", description = "Updates an existing moulding")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Moulding updated successfully"),
            @ApiResponse(responseCode = "404", description = "Moulding not found")
    })
    public EntityResponse<?> updateMoulding(
            @Parameter(description = "ID of the moulding to update") @PathVariable Long id,
            @RequestBody Moulding mouldingDetails) {
        Optional<Moulding> moulding = mouldingService.getMouldingById(id);
        if (moulding.isPresent()) {
            Moulding updatedMoulding = moulding.get();
            updatedMoulding.setName(mouldingDetails.getName());
            updatedMoulding.setSize(mouldingDetails.getSize());
            updatedMoulding.setArticle(mouldingDetails.getArticle());
            updatedMoulding.setPrice(mouldingDetails.getPrice());
            updatedMoulding.setQuantity(mouldingDetails.getQuantity());
            updatedMoulding.setTitle(mouldingDetails.getTitle());
            updatedMoulding.setDescription(mouldingDetails.getDescription());
            updatedMoulding.setImagesUrl(mouldingDetails.getImagesUrl());
            return EntityResponse.success("Moulding updated successfully", mouldingService.saveMoulding(updatedMoulding));
        } else {
            return EntityResponse.error("Moulding not found", null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Delete a moulding", description = "Deletes a moulding by its ID")
    @ApiResponse(responseCode = "200", description = "Moulding deleted successfully")
    public EntityResponse<?> deleteMoulding(@Parameter(description = "ID of the moulding to delete") @PathVariable Long id) {
        mouldingService.deleteMoulding(id);
        return EntityResponse.success("Moulding deleted successfully");
    }
}