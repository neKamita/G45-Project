package uz.pdp.enums;

import lombok.Getter;

/**
 * Available door colors - Where doors get their personality! 🚪✨
 * 
 * Our colors are grouped into families, making it easy to find
 * complementary shades that work well together.
 * 
 * Fun fact: Just like how families stick together, our color families
 * ensure your doors always have matching relatives nearby! 
 * 
 * Color Families:
 * - Light Family: WHITE, IVORY, CREAM
 * - Brown Family: BROWN, DARK_BROWN, LIGHT_BROWN, MAHOGANY, OAK, CHERRY, NATURAL
 * - Grey Family: BLACK, CHARCOAL, DARK_GREY, GREY, LIGHT_GREY
 * - Special: CUSTOM (for custom hex color codes)
 * 
 * @version 1.1
 * @since 2025-02-08
 */
@Getter
public enum Color {
    // Light Family - For doors that want to brighten up your day! ☀️
    WHITE("Classic White"),
    IVORY("Elegant Ivory"),
    CREAM("Warm Cream"),
    
    // Brown Family - Because every door deserves to be naturally beautiful! 🌳
    BROWN("Rich Brown"),
    DARK_BROWN("Deep Dark Brown"),
    LIGHT_BROWN("Gentle Light Brown"),
    MAHOGANY("Classic Mahogany"),
    OAK("Natural Oak"),
    CHERRY("Luxurious Cherry"),
    NATURAL("Pure Natural"),
    
    // Grey Family - For doors with a sophisticated edge! 🎩
    BLACK("Timeless Black"),
    CHARCOAL("Sophisticated Charcoal"),
    DARK_GREY("Bold Dark Grey"),
    GREY("Classic Grey"),
    LIGHT_GREY("Subtle Light Grey"),
    
    // Special - For those who think outside the door frame! 🎨
    CUSTOM("Custom Color");

    private final String displayName;

    /**
     * Creates a new Color enum with a display name.
     * 
     * @param displayName The human-friendly name of the color
     * 
     * Knock knock! Who's there? Your perfectly colored door! 🚪
     */
    Color(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}