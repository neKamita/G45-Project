package uz.pdp.config;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uz.pdp.entity.Door;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.entity.User;
import uz.pdp.enums.*;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.FurnitureDoorRepository;
import uz.pdp.repository.UserRepository;

import java.util.*;

/**
 * The Grand Door Emporium's Data Initializer! 🎭
 * 
 * Warning: This class is responsible for populating our virtual door paradise.
 * Side effects may include:
 * - Excessive door puns
 * - Sudden urge to renovate your house
 * - Uncontrollable desire to collect door handles
 * 
 * Remember: Every door is a gateway to new possibilities! 
 * (Unless it's locked, then it's just a wall with attitude 😅)
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final FurnitureDoorRepository furnitureDoorRepository;
    private final DoorRepository doorRepository;
    private final UserRepository userRepository;
    private final Faker faker = new Faker(new Locale("en-US"));

    // Our premium materials for fancy doors
    private static final String[] DOOR_MATERIALS = {
        "Solid Oak", "Mahogany", "Walnut", "Cherry Wood", "Teak", 
        "Reinforced Steel", "Aluminum", "Composite", "Fiberglass",
        "Bamboo", "Cedar", "Pine", "Maple", "Ash", "Birch",
        "Metal-Wood Hybrid", "Tempered Glass", "Engineered Wood",
        "Reclaimed Wood", "Stainless Steel"
    };

    // Famous door manufacturers (with a twist of humor)
    private static final String[] MANUFACTURERS = {
        "DoorMaster Pro", "Gateway Giants", "Portal Paradise",
        "Knock Knock Inc.", "Swing Kings", "Threshold Legends",
        "Door to Door Luxury", "Entryway Excellence", "Portal Perfection",
        "Doorway Dreams", "The Door Father", "Hinged Heaven",
        "Epic Entries", "Supreme Swings", "Royal Revolvers"
    };

    // Door styles for more variety
    private static final String[] DOOR_STYLES = {
        "Modern", "Classic", "Rustic", "Contemporary", "Traditional",
        "Industrial", "Minimalist", "Vintage", "Art Deco", "Victorian",
        "Colonial", "Mediterranean", "Japanese", "Scandinavian", "Gothic"
    };

    private static final Map<FurnitureType, String[]> MATERIALS = Map.of(
        FurnitureType.HANDLE, new String[]{"Stainless Steel", "Brass", "Bronze", "Chrome", "Gold-Plated", "Nickel"},
        FurnitureType.LOCK, new String[]{"Hardened Steel", "Titanium-Reinforced Steel", "Brass", "Smart Alloy"},
        FurnitureType.HINGES, new String[]{"Stainless Steel", "Steel", "Brass", "Bronze"},
        FurnitureType.AUTOMATIC_CLOSER, new String[]{"Aluminum", "Steel", "Premium Aluminum", "Reinforced Alloy"},
        FurnitureType.NO_FURNITURE, new String[]{"Not Applicable"}
    );

    private static final Map<FurnitureType, String[]> PREFIXES = Map.of(
        FurnitureType.HANDLE, new String[]{"Elegant", "Luxurious", "Modern", "Classic", "Designer", "Premium"},
        FurnitureType.LOCK, new String[]{"SecureMax", "SafeGuard", "SmartLock", "FortKnox", "Guardian"},
        FurnitureType.HINGES, new String[]{"Silent", "Heavy-Duty", "Smooth", "Professional", "Industrial"},
        FurnitureType.AUTOMATIC_CLOSER, new String[]{"Smooth", "Gentle", "Professional", "WhisperClose", "SoftClose"},
        FurnitureType.NO_FURNITURE, new String[]{"Basic"}
    );

    // Real Unsplash photo IDs for doors
    private static final String[] DOOR_IMAGE_IDS = {
        "1417325384643-aac51acc9e5d",  // Elegant wooden door
        "1519167367953-5875af0f6d7b",  // Modern glass door
        "1530268729831-4b0b9e170218",  // Classic white door
        "1534609146522-2b8a3cb8e8f3",  // Rustic door
        "1541450805268-4822a3a774ca"   // Industrial metal door
    };

    // Real Unsplash photo IDs for door hardware
    private static final String[] HARDWARE_IMAGE_IDS = {
        "1503787091259-324336010fc4",  // Bronze handle
        "1516455590571-18256e5bb9ff",  // Modern knob
        "1526887520775-4b14b8aed897",  // Vintage lock
        "1533740566848-5f7fe9c21dd4",  // Steel hinges
        "1541450805268-4822a3a774ca"   // Door accessories
    };

    @Override
    public void run(String... args) {
        if (doorRepository.count() == 0 && furnitureDoorRepository.count() == 0) {
            System.out.println("🎭 Welcome to the Door Paradise Initialization!");
            System.out.println("🏗️ Building your door empire...");
            
            // First, create our legendary door master
            User seller = createSampleSeller();
            System.out.println("👔 Door Master has entered the building!");
            
            // Create the main attractions - the doors!
            initializeSampleDoors(seller);
            
            // Now add some bling - door accessories
            initializeSampleFurnitureDoors();
            
            System.out.println("🎉 Door Paradise is ready for business!");
            System.out.println("🚪 May your doors be sturdy and your handles shiny!");
        } else {
            System.out.println("🏪 Door Paradise is already stocked and ready!");
        }
    }

    private User createSampleSeller() {
        // First, check if our Door Master already exists
        Optional<User> existingUser = userRepository.findByEmail("doormaster@example.com");
        if (existingUser.isPresent()) {
            System.out.println("👋 Welcome back, Door Master!");
            return existingUser.get();
        }

        User seller = new User();
        seller.setName("Door");
        seller.setLastname("Master");
        seller.setEmail("doormaster@example.com");
        seller.setPassword("$2a$12$IKEQb00u5QpZMx4v5zMweu.3wrq0pS7XLCHO4yHZ.BW/yvWu1feo2"); // "password123"
        seller.setPhone("+1234567890"); // Add a default phone number for the Door Master
        seller.setRole(Role.SELLER); // Ensure the role is set
        
        User savedSeller = userRepository.save(seller);
        System.out.println("🎩 A new Door Master has been crowned! 📞 Reachable at: " + seller.getPhone());
        return savedSeller;
    }

    private void initializeSampleDoors(User seller) {
        List<Door> sampleDoors = new ArrayList<>();
        
        // Generate at least 20 sample doors
        int doorCount = faker.number().numberBetween(20, 26); // 20-25 doors
        
        for (int i = 0; i < doorCount; i++) {
            Door door = new Door();
            
            // Generate a fancy door name with style and material
            String style = DOOR_STYLES[faker.number().numberBetween(0, DOOR_STYLES.length)];
            String material = DOOR_MATERIALS[faker.number().numberBetween(0, DOOR_MATERIALS.length)];
            String adjective = faker.commerce().productName().split(" ")[0]; // Get first word as adjective
            
            // 33% chance to add a special prefix
            String prefix = faker.number().numberBetween(0, 3) == 0 ? 
                          faker.commerce().material() + "-Infused " : "";
            
            door.setName(prefix + style + " " + adjective + " " + material + " Door");
            
            // Add a witty description
            door.setDescription(generateDoorDescription(material, style));
            
            // Set random size (75% standard, 25% custom)
            if (faker.number().numberBetween(1, 100) <= 75) {
                door.setSize(Size.values()[faker.number().numberBetween(0, Size.values().length - 1)]); // Excluding CUSTOM
            } else {
                door.setSize(Size.CUSTOM);
                door.setCustomWidth(faker.number().numberBetween(60, 120) * 1.0);
                door.setCustomHeight(faker.number().numberBetween(180, 240) * 1.0);
            }
            
            // Set random color (85% standard, 15% custom)
            door.setColor(Color.values()[faker.number().numberBetween(0, Color.values().length)]);
            door.setIsCustomColor(faker.number().numberBetween(1, 100) <= 15);
            
            // Set door location (where the door will be used)
            door.setDoorLocation(DoorLocation.values()[faker.number().numberBetween(0, DoorLocation.values().length)]);
            
            // Set frame type
            door.setFrameType(FrameType.values()[faker.number().numberBetween(0, FrameType.values().length)]);
            
            // Set hardware type
            door.setHardware(HardwareType.values()[faker.number().numberBetween(0, HardwareType.values().length)]);
            
            door.setMaterial(material);
            door.setManufacturer(MANUFACTURERS[faker.number().numberBetween(0, MANUFACTURERS.length)]);
            
            // Premium materials get longer warranty
            int baseWarranty = material.contains("Steel") || 
                             Arrays.asList("Mahogany", "Teak", "Metal-Wood Hybrid")
                                   .contains(material) ? 5 : 2;
            door.setWarrantyYears(baseWarranty + faker.number().numberBetween(0, 6));
            
            // Price based on material quality and style
            double basePrice = faker.number().numberBetween(20000, 100000) / 100.0;
            // Premium materials cost more
            if (material.contains("Steel") || 
                Arrays.asList("Mahogany", "Teak", "Metal-Wood Hybrid", "Tempered Glass")
                      .contains(material)) {
                basePrice *= 1.5;
            }
            // Premium styles cost more
            if (Arrays.asList("Art Deco", "Victorian", "Japanese", "Gothic")
                      .contains(style)) {
                basePrice *= 1.3;
            }
            door.setPrice(basePrice);
            
            generateDoorImages(door);
            
            door.setStatus(DoorStatus.AVAILABLE);
            door.setSeller(seller);
            door.setActive(true);
            
            sampleDoors.add(door);
        }
        
        // Save all our magnificent doors
        doorRepository.saveAll(sampleDoors);
        
        // Celebrate the door creation!
        System.out.println("🚪 Welcome to the Door Paradise!");
        System.out.println("✨ " + sampleDoors.size() + " magnificent doors have been crafted!");
        System.out.println("\n🎨 Door Showcase:");
        sampleDoors.forEach(door -> 
            System.out.printf("   • %s - $%.2f (by %s, %d year warranty)%n", 
                door.getName(), 
                door.getFinalPrice(),
                door.getManufacturer(),
                door.getWarrantyYears())
        );
    }

    private void generateDoorImages(Door door) {
        List<String> images = new ArrayList<>();
        // Main large image - always a door image
        String mainImageId = DOOR_IMAGE_IDS[faker.number().numberBetween(0, DOOR_IMAGE_IDS.length)];
        images.add(String.format("https://images.unsplash.com/photo-%s?q=80&fm=jpg&w=800&h=1200&fit=crop", mainImageId));
        
        // Add 2-3 additional images
        int additionalImages = faker.number().numberBetween(2, 4);
        for (int i = 0; i < additionalImages; i++) {
            String imageId;
            // Mix of door and hardware images for additional shots
            if (faker.number().numberBetween(0, 2) == 0) {
                imageId = DOOR_IMAGE_IDS[faker.number().numberBetween(0, DOOR_IMAGE_IDS.length)];
            } else {
                imageId = HARDWARE_IMAGE_IDS[faker.number().numberBetween(0, HARDWARE_IMAGE_IDS.length)];
            }
            images.add(String.format("https://images.unsplash.com/photo-%s?q=80&fm=jpg&w=400&h=600&fit=crop", imageId));
        }
        door.setImages(images);
    }

    private String generateDoorDescription(String material, String style) {
        String[] templates = {
            "A %s door that makes your neighbors' doors look like they're trying too hard! Made from premium %s.",
            "This %s masterpiece isn't just a door, it's a conversation starter! Crafted from finest %s.",
            "When %s meets %s, magic happens! Your guests will be too busy admiring it to notice your messy house.",
            "A door so %s, it makes other doors feel inadequate. Premium %s construction for those with exquisite taste.",
            "This %s %s door doesn't just open and close, it makes an entrance! Perfect for dramatic exits too!",
            "Crafted from %s with a touch of %s magic - because your home deserves nothing less!",
            "A %s statement piece that says 'I have arrived!' (Through a very nice %s door, obviously)",
            "The perfect blend of %s style and %s durability. Your neighbors called - they want their envy back!",
            "When %s craftsmanship meets %s design, doors become art. This isn't just a door, it's a masterpiece!",
            "Warning: This %s door with %s finish may cause spontaneous home renovation desires in your guests."
        };
        
        String template = templates[faker.number().numberBetween(0, templates.length)];
        return String.format(template, style.toLowerCase(), material.toLowerCase());
    }

    private void initializeSampleFurnitureDoors() {
        List<FurnitureDoor> sampleDoors = new ArrayList<>();
        
        // Generate sample data for each furniture type
        for (FurnitureType type : FurnitureType.values()) {
            if (type != FurnitureType.NO_FURNITURE) {
                // Generate 3-5 items for each type
                int itemCount = faker.number().numberBetween(3, 6);
                for (int i = 0; i < itemCount; i++) {
                    sampleDoors.add(createRandomDoor(type));
                }
            }
        }

        // Save all our fabulous door accessories
        furnitureDoorRepository.saveAll(sampleDoors);
        
        // Celebrate our door fashion show! 🎉
        System.out.println("🎉 The door accessory fashion show is ready to begin!");
        System.out.println("✨ " + sampleDoors.size() + " stunning pieces are waiting to make doors feel beautiful!");
        System.out.println("💃 Time to turn those boring doors into runway models!");
        
        // Print some sample data for verification
        sampleDoors.forEach(door -> 
            System.out.printf("🎯 Created: %s (%s) - $%.2f%n", 
                door.getName(), 
                door.getFurnitureType(), 
                door.getPrice())
        );
    }

    private FurnitureDoor createRandomDoor(FurnitureType type) {
        String[] materials = MATERIALS.get(type);
        String[] prefixes = PREFIXES.get(type);
        
        String material = materials[faker.number().numberBetween(0, materials.length)];
        String prefix = prefixes[faker.number().numberBetween(0, prefixes.length)];
        
        // Generate dimensions based on type
        String dimensions = switch (type) {
            case HANDLE -> faker.number().numberBetween(150, 300) + "x" + 
                         faker.number().numberBetween(40, 60) + "x" +
                         faker.number().numberBetween(15, 30);
            case LOCK -> faker.number().numberBetween(60, 100) + "x" +
                       faker.number().numberBetween(140, 200) + "x" +
                       faker.number().numberBetween(25, 40);
            case HINGES -> faker.number().numberBetween(60, 120) + "x" +
                         faker.number().numberBetween(40, 70) + "x" +
                         faker.number().numberBetween(3, 8);
            case AUTOMATIC_CLOSER -> faker.number().numberBetween(200, 300) + "x" +
                                  faker.number().numberBetween(50, 70) + "x" +
                                  faker.number().numberBetween(35, 50);
            default -> "0x0x0";
        };

        // Generate price based on material and type
        double basePrice = switch (type) {
            case HANDLE -> faker.number().numberBetween(2999, 19999) / 100.0;
            case LOCK -> faker.number().numberBetween(7999, 39999) / 100.0;
            case HINGES -> faker.number().numberBetween(1999, 5999) / 100.0;
            case AUTOMATIC_CLOSER -> faker.number().numberBetween(9999, 29999) / 100.0;
            default -> 0.0;
        };
        
        // Add premium for special materials
        if (material.contains("Gold") || material.contains("Titanium")) {
            basePrice *= 1.5;
        }

        FurnitureDoor door = new FurnitureDoor();
        door.setName(prefix + " " + faker.commerce().productName());
        door.setMaterial(material);
        door.setDescription(generateDescription(type, material));
        door.setPrice(basePrice);
        door.setDimensions(dimensions);
        door.setStockQuantity(faker.number().numberBetween(10, 100));
        door.setFurnitureType(type);
        
        generateHardwareImages(door);
        
        return door;
    }

    private void generateHardwareImages(FurnitureDoor door) {
        List<String> images = new ArrayList<>();
        // Main product image - use hardware specific images
        String mainImageId = HARDWARE_IMAGE_IDS[faker.number().numberBetween(0, HARDWARE_IMAGE_IDS.length)];
        images.add(String.format("https://images.unsplash.com/photo-%s?q=80&fm=jpg&w=800&h=600&fit=crop", mainImageId));
        
        // Add 1-2 additional detail images
        int additionalImages = faker.number().numberBetween(1, 3);
        for (int i = 0; i < additionalImages; i++) {
            String imageId = HARDWARE_IMAGE_IDS[faker.number().numberBetween(0, HARDWARE_IMAGE_IDS.length)];
            images.add(String.format("https://images.unsplash.com/photo-%s?q=80&fm=jpg&w=400&h=300&fit=crop", imageId));
        }
        door.setImages(images);
    }

    private String generateDescription(FurnitureType type, String material) {
        String[] adjectives = {"stunning", "elegant", "sophisticated", "modern", "classic", "premium"};
        String adj = adjectives[faker.number().numberBetween(0, adjectives.length)];
        
        String baseDesc = switch (type) {
            case HANDLE -> "This %s handle combines style with functionality - Your door's jewelry! %s";
            case LOCK -> "Advanced security meets elegant design - Your door's bodyguard! %s";
            case HINGES -> "Smooth operation meets durability - Your door's yoga instructor! %s";
            case AUTOMATIC_CLOSER -> "Effortless closing with style - Your door's personal butler! %s";
            default -> "%s accessory for your door! %s";
        };
        
        String emoji = switch (type) {
            case HANDLE -> "✨";
            case LOCK -> "🔒";
            case HINGES -> "🔄";
            case AUTOMATIC_CLOSER -> "🚪";
            default -> "🎯";
        };

        String materialDesc = String.format("Crafted from premium %s for lasting beauty and durability.", material);
        
        return String.format(baseDesc, adj, emoji) + " " + materialDesc;
    }
}
