package uz.pdp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Column;
import java.util.Map;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.Socials;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;        // Store or office name
    private String street;      // Street address
    private String city;
    private String phone;
    private String workingHours;
    private String email;
    
    @ElementCollection
    @CollectionTable(name = "address_social_links")
    @MapKeyColumn(name = "social_type")
    @Column(name = "social_link")
    private Map<Socials, String> socialLinks = new HashMap<>();
    
    @OneToOne(mappedBy = "address", cascade = CascadeType.ALL)
    private MapPoint location;  // Geographic coordinates
}
