package uz.pdp.enums;

import lombok.Getter;

/**
 * Enum representing different types of items that can be added to a basket.
 * Each type opens a door to new possibilities! 
 */
@Getter
public enum ItemType {
    DOOR,
    DOOR_ACCESSORY, // Handles, hinges, locks, and other door furniture
    MOULDING       // Decorative trim pieces and frames
}