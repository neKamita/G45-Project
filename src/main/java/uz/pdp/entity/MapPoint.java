package uz.pdp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "map_points")
public class MapPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double latitude;
    private Double longitude;
    
    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;
    
    private String markerTitle;    // Keep this for customizable titles
}
