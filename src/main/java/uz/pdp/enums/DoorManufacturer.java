package uz.pdp.enums;

/**
 * Enum representing door manufacturers.
 * 🏭 The who's who of door-making royalty!
 */
public enum DoorManufacturer {
    DOORMASTER_PRO("DoorMaster Pro"),
    GATEWAY_GIANTS("Gateway Giants"),
    PORTAL_PARADISE("Portal Paradise"),
    KNOCK_KNOCK_INC("Knock Knock Inc."),
    SWING_KINGS("Swing Kings"),
    THRESHOLD_LEGENDS("Threshold Legends"),
    DOOR_TO_DOOR_LUXURY("Door to Door Luxury"),
    ENTRYWAY_EXCELLENCE("Entryway Excellence"),
    PORTAL_PERFECTION("Portal Perfection"),
    DOORWAY_DREAMS("Doorway Dreams"),
    THE_DOOR_FATHER("The Door Father"),
    HINGED_HEAVEN("Hinged Heaven"),
    EPIC_ENTRIES("Epic Entries"),
    SUPREME_SWINGS("Supreme Swings"),
    ROYAL_REVOLVERS("Royal Revolvers");

    private final String displayName;

    DoorManufacturer(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DoorManufacturer fromDisplayName(String displayName) {
        for (DoorManufacturer manufacturer : values()) {
            if (manufacturer.getDisplayName().equalsIgnoreCase(displayName)) {
                return manufacturer;
            }
        }
        throw new IllegalArgumentException("No manufacturer found for display name: " + displayName);
    }
}
