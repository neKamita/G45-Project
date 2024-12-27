package uz.pdp.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.role.Socials;
import uz.pdp.entity.Address;
import java.time.LocalDate;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToOne(cascade = CascadeType.ALL)
    private Address address;
    private String email;
    private String phone;
    private LocalDate workTime;
    @OneToOne(cascade = CascadeType.ALL)
    private List<Socials> socialsList;
}
