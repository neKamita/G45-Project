package uz.pdp.enums;

import lombok.Getter;

/**
 * Available door colors.
 * Because life's too short for boring doors! 
 * 
 * From classic elegance to bold statements,
 * we've got every shade a door could dream of.
 * 
 * Fun fact: Studies show that door color can affect
 * home value by up to 5%! Choose wisely! 
 */
@Getter
public enum Color {
    // Classic shades
    WHITE("Classic White"),
    OFF_WHITE("Off White"),
    CREAM("Cream"),
    IVORY("Ivory"),
    
    // Wood tones
    LIGHT_OAK("Light Oak"),
    GOLDEN_OAK("Golden Oak"),
    DARK_OAK("Dark Oak"),
    WALNUT("Walnut"),
    MAHOGANY("Mahogany"),
    TEAK("Teak"),
    CHERRY("Cherry"),
    ESPRESSO("Espresso"),
    
    // Gray scale
    LIGHT_GRAY("Light Gray"),
    GRAY("Gray"),
    CHARCOAL("Charcoal"),
    BLACK("Black"),
    
    // Earth tones
    BEIGE("Beige"),
    TAN("Tan"),
    BROWN("Brown"),
    BRONZE("Bronze"),
    
    // Bold colors
    NAVY("Navy Blue"),
    FOREST_GREEN("Forest Green"),
    WINE_RED("Wine Red"),
    BURGUNDY("Burgundy"),
    
    // Modern finishes
    GUNMETAL("Gunmetal"),
    BRUSHED_STEEL("Brushed Steel"),
    ANTIQUE_BRASS("Antique Brass"),
    
    // Special
    CUSTOM("Custom Color");

    private final String displayName;

    Color(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}