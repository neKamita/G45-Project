package uz.pdp.mutations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.dto.DoorDto;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoorConfigInput {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Size size;
    private Color color;
    private String material;
    private String manufacturer;
    private Integer warrantyYears;
    private Double width;
    private Double height;
    
    public DoorDto toDoorDTO() {
        DoorDto dto = new DoorDto();
        dto.setName(this.name);
        dto.setDescription(this.description);
        dto.setPrice(this.price);
        dto.setSize(this.size);
        dto.setColor(this.color);
        dto.setMaterial(this.material);
        dto.setManufacturer(this.manufacturer);
        dto.setWarrantyYears(this.warrantyYears);
        dto.setCustomWidth(this.width);
        dto.setCustomHeight(this.height);
        return dto;
    }
}