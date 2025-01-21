package uz.pdp.enums;

/**
 * Enumeration of possible door frame types.
 * 
 * The unsung heroes that hold our doors! 🖼️🚪
 */
public enum FrameType {
    STANDARD("Standard"),
    TELESCOPIC("Telescopic"),
    HIDDEN("Hidden"),
    REBATED("Rebated"),
    NON_REBATED("Non-rebated");

    private final String displayName;

    FrameType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
