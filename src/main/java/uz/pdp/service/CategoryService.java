package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.dto.CategoryDTO;
import uz.pdp.entity.Category;
import uz.pdp.exception.DuplicateResourceException;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing door categories.
 * 
 * Where categories come to life - it's like a category factory, but with more swagger! 🏭✨
 */
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Creates a new category.
     * 
     * @param categoryDTO the category data
     * @return the created category
     * @throws DuplicateResourceException if category with same name exists
     */
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) throws DuplicateResourceException {
        categoryRepository.findByNameIgnoreCase(categoryDTO.getName())
                .ifPresent(c -> {
                    try {
                        throw new DuplicateResourceException("Category with name " + categoryDTO.getName() + " already exists!");
                    } catch (DuplicateResourceException e) {
                        throw new RuntimeException(e);
                    }
                });

        Category category = Category.builder()
                .name(categoryDTO.getName())
                .active(true)
                .build();

        return mapToDTO(categoryRepository.save(category));
    }

    /**
     * Updates an existing category.
     * 
     * @param id the category ID
     * @param categoryDTO the updated category data
     * @return the updated category
     * @throws ResourceNotFoundException if category not found
     * @throws DuplicateResourceException if category with same name exists
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) throws ResourceNotFoundException, DuplicateResourceException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.findByNameIgnoreCase(categoryDTO.getName())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        try {
                            throw new DuplicateResourceException("Category with name " + categoryDTO.getName() + " already exists!");
                        } catch (DuplicateResourceException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        category.setName(categoryDTO.getName());
        category.setActive(categoryDTO.isActive());  
        
        return mapToDTO(categoryRepository.save(category));
    }

    /**
     * Retrieves a category by ID.
     * 
     * @param id the category ID
     * @return the category
     * @throws ResourceNotFoundException if category not found
     */
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    /**
     * Lists all active categories.
     * 
     * @return list of active categories
     */
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Soft deletes a category.
     * 
     * @param id the category ID
     * @throws ResourceNotFoundException if category not found
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .active(category.isActive())
                .build();
    }
}
