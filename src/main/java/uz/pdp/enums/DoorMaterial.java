package uz.pdp.enums;

/**
 * Enum representing different door materials.
 * 🪚 Because not all doors are created equal! 
 */
public enum DoorMaterial {
    SOLID_OAK("Solid Oak"),
    MAHOGANY("Mahogany"),
    WALNUT("Walnut"),
    CHERRY_WOOD("Cherry Wood"),
    TEAK("Teak"),
    REINFORCED_STEEL("Reinforced Steel"),
    ALUMINUM("Aluminum"),
    COMPOSITE("Composite"),
    FIBERGLASS("Fiberglass"),
    BAMBOO("Bamboo"),
    CEDAR("Cedar"),
    PINE("Pine"),
    MAPLE("Maple"),
    ASH("Ash"),
    BIRCH("Birch"),
    METAL_WOOD_HYBRID("Metal-Wood Hybrid"),
    TEMPERED_GLASS("Tempered Glass"),
    ENGINEERED_WOOD("Engineered Wood"),
    RECLAIMED_WOOD("Reclaimed Wood"),
    STAINLESS_STEEL("Stainless Steel");

    private final String displayName;

    DoorMaterial(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DoorMaterial fromDisplayName(String displayName) {
        for (DoorMaterial material : values()) {
            if (material.getDisplayName().equalsIgnoreCase(displayName)) {
                return material;
            }
        }
        throw new IllegalArgumentException("No material found for display name: " + displayName);
    }
}
