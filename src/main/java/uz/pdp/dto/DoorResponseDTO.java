package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.Color;
import uz.pdp.enums.DoorStatus;
import uz.pdp.enums.FurnitureType;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Door entity responses.
 * Because every door deserves a proper introduction! 🚪✨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoorResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String dimensions;
    private Integer stockQuantity;
    private DoorStatus status;
    private Color color;
    private FurnitureType furnitureType;
    private CategoryDTO category;
    private List<String> images;
}
