package uz.pdp.mutations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoorConfigInput {
    private Size size;
    private Color color;
    private Double width;
    private Double height;
}