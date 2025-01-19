package uz.pdp.enums;

/**
 * Enumeration of furniture types available for doors.
 * The fashion catalog for doors - because doors need accessories too! 
 * 
 * From locks that keep secrets to handles that welcome guests,
 * every door deserves to be dressed to impress! 
 */
public enum FurnitureType {
    /**
     * Lock - For when you need to keep your door's secrets safe 
     */
    LOCK("Qulf"),

    /**
     * Handle - The door's handshake with the world 
     */
    HANDLE("Tutqich"),

    /**
     * Hinges - The door's yoga instructor - keeps it flexible! 
     */
    HINGES("Ilkagi"),

    /**
     * Automatic Closer - The lazy door's best friend 
     */
    AUTOMATIC_CLOSER("Avtomatik yopuvchi"),

    /**
     * No Furniture - The minimalist door's dream come true 
     */
    NO_FURNITURE("Furniturasiz");

    private final String uzName;

    /**
     * Constructor for furniture types.
     * @param uzName The Uzbek name of the furniture type
     * 
     * Bilingual doors are the future! 
     */
    FurnitureType(String uzName) {
        this.uzName = uzName;
    }

    /**
     * Gets the Uzbek name of the furniture type.
     * @return The localized name in Uzbek
     * 
     * Because even doors need to speak the local language! 
     */
    public String getUzName() {
        return uzName;
    }
}
