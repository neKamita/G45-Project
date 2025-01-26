package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MouldingDTO {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        @NotBlank(message = "Size is required")
        private String size;
        @NotBlank(message = "Article code is required")
        private String article;
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        private Double price;
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        private Integer quantity;
        private Double priceOverall;
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        private List<String> imagesUrl;

        public void calculatePriceOverall() {
            if (price != null && quantity != null) {
                this.priceOverall = price * quantity;
            } else {
                this.priceOverall = 0.0;
            }
        }
}


