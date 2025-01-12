package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DoorDto {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
    
    @JsonIgnore // Ignore during JSON serialization
    private List<MultipartFile> imageFiles;
    
    private List<String> images;
    
    @NotNull(message = "Size is required")
    private Size size;
    
    @NotNull(message = "Color is required")
    private Color color;
    
    private String material;
    private String manufacturer;
    private Integer warrantyYears;
    
    private Double customWidth;
    private Double customHeight;
    private Boolean isCustomColor = false;
}