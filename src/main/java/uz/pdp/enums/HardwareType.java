package uz.pdp.enums;

/**
 * Enum representing different types of door hardware.
 * 🔧 The nuts and bolts of door personality! 
 */
public enum HardwareType {
    STANDARD_HINGES("Standard Hinges"),
    PIVOT("Pivot"),
    SLIDING("Sliding"),
    POCKET("Pocket"),
    BARN_DOOR("Barn Door"),
    BI_FOLD("Bi-Fold"),
    CONCEALED_HINGES("Concealed Hinges"),
    SPRING_HINGES("Spring Hinges"),
    DOUBLE_ACTION("Double Action"),
    AUTOMATIC("Automatic");

    private final String displayName;

    HardwareType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static HardwareType fromDisplayName(String displayName) {
        for (HardwareType type : values()) {
            if (type.getDisplayName().equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No hardware type found for display name: " + displayName);
    }
}
