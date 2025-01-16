package uz.pdp.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

@Data
public class UserDoorHistoryDto {
    private UserBasicInfo user;
    private List<DoorHistoryEntry> history;

    @Data
    public static class DoorHistoryEntry {
        private Long id;
        private LocalDateTime accessedAt;
        private DoorBasicInfo door;
    }

    @Data
    public static class UserBasicInfo {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    public static class DoorBasicInfo {
        private Long id;
        private String name;
        private Double price;
        private Double finalPrice;
        private String size;
        private String color;
        private String material;
        private Long sellerId;
    }
}

