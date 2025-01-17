package uz.pdp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OrderDto {
    @NotNull(message = "Door ID is required")
    private Long doorId;
    
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    private String deliveryAddress;
    private String contactPhone;
    
    public enum OrderType {
        FULL_SET,    // Complete door set
        POLOTNO     // Door canvas only
    }
}
