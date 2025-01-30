package uz.pdp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor 
@Table(name = "locations", indexes = {
    @Index(name = "idx_location_coords", columnList = "latitude,longitude"),
    @Index(name = "idx_location_title", columnList = "markerTitle")
})
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double latitude;
    private Double longitude;
    private String markerTitle;

    @OneToOne(mappedBy = "location")
    @JsonBackReference
    private Address address;
}