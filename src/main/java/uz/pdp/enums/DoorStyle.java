package uz.pdp.enums;

/**
 * Enum representing different door styles.
 * 🎨 Because style is the door to personality!
 */
public enum DoorStyle {
    MODERN("Modern"),
    CLASSIC("Classic"),
    RUSTIC("Rustic"),
    CONTEMPORARY("Contemporary"),
    TRADITIONAL("Traditional"),
    INDUSTRIAL("Industrial"),
    MINIMALIST("Minimalist"),
    VINTAGE("Vintage"),
    ART_DECO("Art Deco"),
    VICTORIAN("Victorian"),
    COLONIAL("Colonial"),
    MEDITERRANEAN("Mediterranean"),
    JAPANESE("Japanese"),
    SCANDINAVIAN("Scandinavian"),
    GOTHIC("Gothic");

    private final String displayName;

    DoorStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DoorStyle fromDisplayName(String displayName) {
        for (DoorStyle style : values()) {
            if (style.getDisplayName().equalsIgnoreCase(displayName)) {
                return style;
            }
        }
        throw new IllegalArgumentException("No style found for display name: " + displayName);
    }
}
