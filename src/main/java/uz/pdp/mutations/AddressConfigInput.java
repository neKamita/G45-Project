package uz.pdp.mutations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.Socials;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressConfigInput {
    private String name;
    private String street;
    private String city;
    private String phone;
    private String workingHours;
    private String email;
    private Double latitude;
    private Double longitude;
    private Map<Socials, String> socialLinks;
}