package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.payload.EntityResponse;
import uz.pdp.dto.CustomEnumValueDto;
import uz.pdp.entity.CustomEnumValue;
import uz.pdp.service.EnumManagementService;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing custom enum values.
 * 🎭 Where admins and sellers become enum artists!
 * 
 * Allows adding, viewing, and managing custom enum values for:
 * - Door Materials 🪚 (e.g., "Solid Oak", "Mahogany")
 * - Door Styles 🎨 (e.g., "Modern", "Victorian")
 * - Manufacturers 🏭 (e.g., "DoorMaster Pro", "Portal Paradise")
 * - Hardware Types 🔧 (e.g., "Pivot", "Sliding")
 * 
 * Note: All operations are tied to the authenticated user. No need to specify
 * the creator - we know who you are! 🕵️‍♂️
 */
@RestController
@RequestMapping("/api/v1/enums")
@RequiredArgsConstructor
public class EnumManagementController {
    private final EnumManagementService enumManagementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(
        summary = "Add a new custom enum value", 
        description = "Allows admins and sellers to add new custom enum values. Creator is automatically set to the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Custom enum value added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or enum type"),
        @ApiResponse(responseCode = "403", description = "Not authorized to add enum values")
    })
    public ResponseEntity<EntityResponse<CustomEnumValue>> addCustomEnumValue(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Custom enum value details. Creator and timestamps are set automatically.",
                required = true,
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = CustomEnumValueDto.class,
                        example = """
                        {
                          "enumType": "DoorMaterial",
                          "displayName": "Carbon Fiber"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody CustomEnumValueDto dto) {
        try {
            CustomEnumValue saved = enumManagementService.addCustomEnumValue(dto.toEntity());
            return ResponseEntity.ok(EntityResponse.success(
                "Custom enum value added successfully! 🎨 Your door options just got more interesting!",
                saved
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(EntityResponse.error(
                "Oops! " + e.getMessage() + " 🚫 Please check your input and try again. " +
                "Example: { \"enumType\": \"DoorMaterial\", \"displayName\": \"Carbon Fiber\" }",
                null
            ));
        }
    }

    @GetMapping("/{enumType}")
    @Operation(summary = "Get all values for an enum type", description = "Retrieves all values (built-in + custom) for the specified enum type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved enum values"),
        @ApiResponse(responseCode = "400", description = "Invalid enum type provided")
    })
    public ResponseEntity<EntityResponse<List<String>>> getAllEnumValues(
            @Parameter(description = "Type of enum to retrieve", required = true, 
                      schema = @Schema(allowableValues = {"DoorMaterial", "DoorStyle", "DoorManufacturer", "HardwareType"},
                                    example = "DoorMaterial"))
            @PathVariable String enumType) {
        try {
            List<String> values = enumManagementService.getAllEnumValues(enumType);
            return ResponseEntity.ok(EntityResponse.success(
                String.format("Found %d options for %s! 🎯", values.size(), enumType),
                values
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(EntityResponse.error(
                "Invalid enum type! 🤔 Available types are: DoorMaterial, DoorStyle, DoorManufacturer, HardwareType. " +
                "Example: GET /api/v1/enums/DoorMaterial",
                null
            ));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Deactivate a custom enum value", 
        description = "Allows admins to deactivate (soft delete) a custom enum value. Only the admin can perform this operation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Custom enum value deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Custom enum value not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to deactivate enum values")
    })
    public ResponseEntity<EntityResponse<Void>> deactivateEnumValue(
            @Parameter(description = "ID of the custom enum value to deactivate", required = true)
            @PathVariable Long id) {
        try {
            enumManagementService.deactivateEnumValue(id);
            return ResponseEntity.ok(EntityResponse.success(
                "Custom enum value successfully retired! 👋 It served us well.",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
