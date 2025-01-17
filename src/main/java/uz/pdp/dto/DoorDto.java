package uz.pdp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

@Data
@Getter
@Setter
public class DoorDto {
    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Description is required")
    private String description;

    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Size is required")
    private Size size;

    @NotNull(message = "Color is required")
    private Color color;

    private String material;
    private String manufacturer;
    private Integer warrantyYears;
    private Double customWidth;
    private Double customHeight;
    private Boolean isCustomColor;
}