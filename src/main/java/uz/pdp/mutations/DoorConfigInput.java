package uz.pdp.mutations;

import lombok.Data;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

@Data
public class DoorConfigInput {
    private Long id;
    private Size size;
    private Color color;
    private Double width;
    private Double height;
}