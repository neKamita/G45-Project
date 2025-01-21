package uz.pdp.enums;

/**
 * Enumeration of possible door locations.
 * 
 * Every door needs a home, and these are all the places they might live! 🏠🚪
 */
public enum DoorLocation {
    WINDOW("Window"),
    ROOM("Room"),
    BATHROOM("Bathroom"),
    KITCHEN("Kitchen"),
    ENTRANCE("Entrance"),
    BEDROOM("Bedroom"),
    LIVING_ROOM("Living Room"),
    BALCONY("Balcony"),
    GARAGE("Garage");

    private final String displayName;

    DoorLocation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
