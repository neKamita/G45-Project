package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.CategoryDTO;
import uz.pdp.exception.DuplicateResourceException;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.CategoryService;

import java.util.List;

/**
 * REST controller for managing door categories.
 * 
 * Every door needs a home, and every category is like a cozy doorway to organize our doors! 
 * This controller handles all the CRUD operations for door categories, with a sprinkle of security
 * to make sure only admins can make changes.
 *
 * @version 1.0
 * @since 2025-01-29
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "APIs for managing door categories - where doors find their perfect home! ")
@Validated
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Creates a new category (Admin only).
     * 
     * Like a master carpenter crafting a new doorframe, this endpoint creates a new category
     * for our wonderful doors to call home! 
     *
     * @param categoryDTO the category to create
     * @return EntityResponse containing the created category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new category", description = "Admin only - Create a new category for organizing doors")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category data"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create categories"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public EntityResponse<CategoryDTO> createCategory(
            @Parameter(description = "Category data", required = true) 
            @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO created = categoryService.createCategory(categoryDTO);
            return new EntityResponse<>(
                "Welcome to the family! New category '" + created.getName() + "' is ready for doors! ",
                true,
                created
            );
        } catch (DuplicateResourceException e) {
            log.warn("Attempt to create duplicate category: {}", categoryDTO.getName(), e);
            return new EntityResponse<>(
                "Oops! A category named '" + categoryDTO.getName() + "' already exists. Each door category needs a unique name! ",
                false,
                null
            );
        } catch (AccessDeniedException e) {
            log.warn("Unauthorized attempt to create category", e);
            return new EntityResponse<>(
                "Hold up! Only our master carpenters (admins) can create new categories! ",
                false,
                null
            );
        } catch (Exception e) {
            log.error("Error creating category", e);
            return new EntityResponse<>(
                "Our category workshop is experiencing technical difficulties. Please try again later! ",
                false,
                null
            );
        }
    }

    /**
     * Updates an existing category (Admin only).
     * 
     * Time for some home improvement! This endpoint helps you renovate your category
     * with a fresh coat of paint (new name)! 
     *
     * @param id the category ID
     * @param categoryDTO the updated category data
     * @return EntityResponse containing the updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing category", description = "Admin only - Update an existing category's details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category data"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update categories"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category name conflicts with existing category")
    })
    public EntityResponse<CategoryDTO> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated category data", required = true) 
            @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updated = categoryService.updateCategory(id, categoryDTO);
            return new EntityResponse<>(
                "Category '" + updated.getName() + "' has been beautifully renovated! ",
                true,
                updated
            );
        } catch (ResourceNotFoundException e) {
            log.warn("Attempt to update non-existent category: {}", id, e);
            return new EntityResponse<>(
                "We looked everywhere, but couldn't find a category with ID " + id + ". Did it take a vacation? ",
                false,
                null
            );
        } catch (DuplicateResourceException e) {
            log.warn("Attempt to update category with duplicate name: {}", categoryDTO.getName(), e);
            return new EntityResponse<>(
                "The name '" + categoryDTO.getName() + "' is already taken. Our categories are like snowflakes - each needs a unique name! ",
                false,
                null
            );
        } catch (AccessDeniedException e) {
            log.warn("Unauthorized attempt to update category", e);
            return new EntityResponse<>(
                "Nice try! But only our master carpenters (admins) can renovate categories! ",
                false,
                null
            );
        } catch (Exception e) {
            log.error("Error updating category", e);
            return new EntityResponse<>(
                "Our renovation tools are acting up. Please try again later! ",
                false,
                null
            );
        }
    }

    /**
     * Retrieves a category by ID.
     * 
     * Peek through the peephole to see what's behind this category ID! 
     *
     * @param id the category ID
     * @return EntityResponse containing the requested category
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Open to all users - Retrieve details of a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category found successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public EntityResponse<CategoryDTO> getCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            return new EntityResponse<>(
                "Found it! Category '" + category.getName() + "' is right here! ",
                true,
                category
            );
        } catch (ResourceNotFoundException e) {
            log.warn("Category not found: {}", id, e);
            return new EntityResponse<>(
                "We looked high and low, but category #" + id + " seems to be playing hide and seek! ",
                false,
                null
            );
        } catch (Exception e) {
            log.error("Error retrieving category", e);
            return new EntityResponse<>(
                "Our category finder is having a coffee break. Please try again later! ",
                false,
                null
            );
        }
    }

    /**
     * Lists all active categories.
     * 
     * Take a grand tour of all our door categories! It's like a door museum,
     * but without the squeaky hinges! 
     *
     * @return EntityResponse containing list of all active categories
     */
    @GetMapping
    @Operation(summary = "List all categories", description = "Open to all users - Get a list of all active categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public EntityResponse<List<CategoryDTO>> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                return new EntityResponse<>(
                    "Our category showroom is empty! Time to create some new categories! ",
                    true,
                    categories
                );
            }
            return new EntityResponse<>(
                "Welcome to our grand category exhibition! We have " + categories.size() + " beautiful categories on display! ",
                true,
                categories
            );
        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return new EntityResponse<>(
                "Our category catalog is taking a brief nap. Please come back in a moment! ",
                false,
                null
            );
        }
    }

    /**
     * Deletes a category (Admin only).
     * 
     * Sometimes doors need to find a new home. This endpoint helps retire a category,
     * but don't worry - it's just moving to a better place! 
     *
     * @param id the category ID to delete
     * @return EntityResponse with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Admin only - Soft delete a category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete categories"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public EntityResponse<Void> deleteCategory(
            @Parameter(description = "Category ID to delete", required = true) @PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return new EntityResponse<>(
                "Category #" + id + " has been gracefully retired. Thanks for all the memories! ",
                true,
                null
            );
        } catch (ResourceNotFoundException e) {
            log.warn("Attempt to delete non-existent category: {}", id, e);
            return new EntityResponse<>(
                "We can't retire category #" + id + " because it doesn't exist! It's like trying to close a door that's not there! ",
                false,
                null
            );
        } catch (AccessDeniedException e) {
            log.warn("Unauthorized attempt to delete category", e);
            return new EntityResponse<>(
                "Hold the door! Only our master carpenters (admins) can retire categories! ",
                false,
                null
            );
        } catch (Exception e) {
            log.error("Error deleting category", e);
            return new EntityResponse<>(
                "Our category retirement service is temporarily unavailable. Please try again later! ",
                false,
                null
            );
        }
    }
}
