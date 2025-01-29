package uz.pdp.config;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.pdp.entity.Address;
import uz.pdp.entity.Category;
import uz.pdp.entity.Door;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.entity.Location;
import uz.pdp.entity.Moulding;
import uz.pdp.entity.User;
import uz.pdp.enums.*;
import uz.pdp.repository.AddressRepository;
import uz.pdp.repository.CategoryRepository;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.FurnitureDoorRepository;
import uz.pdp.repository.MouldingRepository;
import uz.pdp.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final MouldingRepository mouldingRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
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

    // Moulding-specific constants
    private static final String[] MOULDING_MATERIALS = {
        "MDF", "Pine", "Oak", "Polyurethane", "PVC",
        "Hardwood", "Softwood", "Composite", "Plastic",
        "Aluminum", "Foam", "Flexible PVC"
    };

    private static final String[] MOULDING_STYLES = {
        "Crown Moulding", "Baseboard", "Chair Rail", "Picture Rail",
        "Casing", "Panel Moulding", "Cove Moulding", "Dentil",
        "Egg and Dart", "Rope Moulding", "Bead Moulding"
    };

    private static final String[] MOULDING_SIZES = {
        "75*38", "100*25", "150*18",
        "120*40", "90*30", "60*20",
        "80*35  ", "110*45"
    };

    // Moulding image IDs from Unsplash
    private static final String[] MOULDING_IMAGE_IDS = {
        "1600585152220-90363fe7e115",  // Classic crown moulding
        "1600573472550-8d929c783b6d",  // Modern baseboard
        "1600571124505-01193eeb16cd",  // Decorative panel moulding
        "1600585152915-d9d4b702c6bd",  // Elegant chair rail
        "1600585153490-76fb20a32601"   // Contemporary casing
    };

    // Store locations with a door theme!
    private static final String[][] STORE_LOCATIONS = {
        // City, Street, Name, Working Hours, Phone, Email, Latitude, Longitude
        {"New York", "Portal Plaza 123", "Door Paradise NYC", "9:00 AM - 6:00 PM", "+1-212-555-0123", "nyc@doorparadise.com", "40.7128", "-74.0060"},
        {"Los Angeles", "Doorway Drive 456", "Door Paradise LA", "8:00 AM - 7:00 PM", "+1-310-555-0124", "la@doorparadise.com", "34.0522", "-118.2437"},
        {"Chicago", "Threshold Avenue 789", "Door Paradise Chicago", "9:00 AM - 5:00 PM", "+1-312-555-0125", "chicago@doorparadise.com", "41.8781", "-87.6298"},
        {"Houston", "Hinge Highway 321", "Door Paradise Houston", "8:30 AM - 6:30 PM", "+1-713-555-0126", "houston@doorparadise.com", "29.7604", "-95.3698"},
        {"Phoenix", "Knocker Lane 654", "Door Paradise Phoenix", "8:00 AM - 5:00 PM", "+1-602-555-0127", "phoenix@doorparadise.com", "33.4484", "-112.0740"},
        {"Philadelphia", "Frame Street 987", "Door Paradise Philly", "9:00 AM - 6:00 PM", "+1-215-555-0128", "philly@doorparadise.com", "39.9526", "-75.1652"},
        {"San Antonio", "Jamb Junction 147", "Door Paradise SA", "8:00 AM - 7:00 PM", "+1-210-555-0129", "sa@doorparadise.com", "29.4241", "-98.4936"},
        {"San Diego", "Lock Loop 258", "Door Paradise SD", "9:00 AM - 5:00 PM", "+1-619-555-0130", "sd@doorparadise.com", "32.7157", "-117.1611"},
        {"Dallas", "Entry Expressway 369", "Door Paradise Dallas", "8:30 AM - 6:30 PM", "+1-214-555-0131", "dallas@doorparadise.com", "32.7767", "-96.7970"},
        {"San Jose", "Gateway Grove 741", "Door Paradise SJ", "8:00 AM - 5:00 PM", "+1-408-555-0132", "sj@doorparadise.com", "37.3382", "-121.8863"}
    };

    @Override
    public void run(String... args) {
        System.out.println("🎭 Welcome to the Door Paradise Initialization!");
        
        // Initialize admin if needed
        if (!userRepository.findByName("etadoor").isPresent()) {
            System.out.println("👑 Crowning the Admin of Door Paradise...");
            initializeDefaultAdmin();
            System.out.println("✨ Admin has been crowned successfully!");
        }
        
        // Initialize categories if needed
        if (categoryRepository.count() == 0) {
            System.out.println("📁 Creating door categories...");
            createDefaultCategories();
            System.out.println("✨ Door categories have been organized!");
        }
        
        // Initialize default doors if needed
        if (doorRepository.count() == 0) {
            System.out.println("🚪 Creating default door collection...");
            createDefaultDoors();
            System.out.println("✨ Default doors have been crafted!");
        }
        
        // Initialize sample doors if needed
        User seller = null;
        if (!userRepository.findByEmail("doormaster@example.com").isPresent()) {
            System.out.println("👔 Appointing the Door Master...");
            seller = createSampleSeller();
            System.out.println("✨ Door Master has entered the building!");
        } else {
            seller = userRepository.findByEmail("doormaster@example.com").get();
        }
        
        if (doorRepository.count() < 20) { // Assuming we want at least 20 sample doors
            System.out.println("🏗️ Creating sample door collection...");
            initializeSampleDoors(seller);
            System.out.println("✨ Sample doors have been installed!");
        }
        
        // Initialize door accessories if needed
        if (furnitureDoorRepository.count() == 0) {
            System.out.println("🔨 Adding door accessories to the catalog...");
            initializeSampleFurnitureDoors();
            System.out.println("✨ Door accessories have been added successfully!");
        }
        
        // Initialize mouldings if needed
        if (mouldingRepository.count() == 0) {
            System.out.println("🖼️ Creating moulding collection...");
            initializeSampleMouldings(seller);
            System.out.println("✨ Mouldings have been crafted perfectly!");
        }
        
        // Initialize store addresses if needed
        if (addressRepository.count() == 0) {
            System.out.println("🏪 Setting up store locations...");
            initializeSampleAddresses();
            System.out.println("✨ Store locations are ready for business!");
        }
        
        System.out.println("🎉 Door Paradise initialization complete!");
        System.out.println("🚪 May your doors be sturdy and your handles shiny!");
    }

    /**
     * Initializes the default admin account.
     * Every kingdom needs a ruler, and our door empire is no exception! 👑
     */
    private void initializeDefaultAdmin() {
        if (!userRepository.findByName("etadoor").isPresent()) {
            User admin = new User();
            admin.setName("etadoor");
            admin.setEmail("admin@etadoor.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setLastname("Admin");
            admin.setPhone("+1234567890");
            
            userRepository.save(admin);
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
        
        // Generate base door models
        int doorCount = faker.number().numberBetween(10, 16); // Reduced count since we'll add variants
        
        for (int i = 0; i < doorCount; i++) {
            Door door = createBaseDoor(seller);
            door.setIsBaseModel(true); // Mark as base model
            generateDoorImages(door); // Use our image generator
            door = doorRepository.save(door);
            
            // Create 2-4 color variants for each base door
            int variantCount = faker.number().numberBetween(2, 5);
            Set<Color> usedColors = new HashSet<>();
            usedColors.add(door.getColor()); // Add base door color
            door.getAvailableColors().add(door.getColor());
            
            for (int j = 0; j < variantCount; j++) {
                // Pick a random color that hasn't been used for this door
                Color variantColor;
                do {
                    variantColor = Color.values()[faker.number().numberBetween(0, Color.values().length)];
                } while (usedColors.contains(variantColor));
                
                usedColors.add(variantColor);
                
                Door variant = new Door();
                copyDoorProperties(door, variant);
                variant.setColor(variantColor);
                variant.setBaseModelId(door.getId());
                variant.setIsBaseModel(false);
                generateDoorImages(variant); // Generate new images for variant
                variant = doorRepository.save(variant);
                
                // Update available colors on base model
                door.getAvailableColors().add(variantColor);
            }
            
            // Save base door with updated available colors
            doorRepository.save(door);
            
            // Add custom color variant for 30% of doors
            if (faker.number().numberBetween(1, 100) <= 30) {
                Door customVariant = new Door();
                copyDoorProperties(door, customVariant);
                customVariant.setCustomColorCode(generateRandomHexColor());
                customVariant.setIsCustomColor(true);
                customVariant.setBaseModelId(door.getId());
                customVariant.setIsBaseModel(false);
                generateDoorImages(customVariant); // Generate new images for custom variant
                doorRepository.save(customVariant);
            }
            
            sampleDoors.add(door);
        }
        
        // Celebrate the door creation!
        System.out.println("🚪 Welcome to the Door Paradise!");
        System.out.println("✨ Created " + sampleDoors.size() + " base door models with their color variants!");
        System.out.println("\n🎨 Door Showcase:");
        sampleDoors.forEach(door -> {
            int variantCount = door.getAvailableColors().size();
            System.out.printf("   • %s - $%.2f (%d color variants)%n", 
                door.getName(), 
                door.getFinalPrice(),
                variantCount);
        });
    }
    
    /**
     * Creates a base door with random properties.
     * Every door needs a good foundation! 🏗️
     */
    private Door createBaseDoor(User seller) {
        Door door = new Door();
        
        // Generate a fancy door name with style and material
        String style = DOOR_STYLES[faker.number().numberBetween(0, DOOR_STYLES.length)];
        String material = DOOR_MATERIALS[faker.number().numberBetween(0, DOOR_MATERIALS.length)];
        String adjective = faker.commerce().productName().split(" ")[0];
        
        String prefix = faker.number().numberBetween(0, 3) == 0 ? 
                      faker.commerce().material() + "-Infused " : "";
        
        door.setName(prefix + style + " " + adjective + " " + material + " Door");
        door.setDescription(generateDoorDescription(material, style));
        
        // Set random size (75% standard, 25% custom)
        if (faker.number().numberBetween(1, 100) <= 75) {
            door.setSize(Size.values()[faker.number().numberBetween(0, Size.values().length - 1)]);
        } else {
            door.setSize(Size.CUSTOM);
            door.setCustomWidth(faker.number().numberBetween(60, 120) * 1.0);
            door.setCustomHeight(faker.number().numberBetween(180, 240) * 1.0);
        }
        
        door.setColor(Color.values()[faker.number().numberBetween(0, Color.values().length)]);
        door.setDoorLocation(DoorLocation.values()[faker.number().numberBetween(0, DoorLocation.values().length)]);
        door.setFrameType(FrameType.values()[faker.number().numberBetween(0, FrameType.values().length)]);
        door.setHardware(HardwareType.values()[faker.number().numberBetween(0, HardwareType.values().length)]);
        
        door.setMaterial(material);
        door.setManufacturer(MANUFACTURERS[faker.number().numberBetween(0, MANUFACTURERS.length)]);
        
        int baseWarranty = material.contains("Steel") || 
                         Arrays.asList("Mahogany", "Teak", "Metal-Wood Hybrid")
                               .contains(material) ? 5 : 2;
        door.setWarrantyYears(baseWarranty + faker.number().numberBetween(0, 6));
        
        // Price calculation with BigDecimal for precision
        BigDecimal basePrice = BigDecimal.valueOf(faker.number().numberBetween(20000, 100000))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
        if (material.contains("Steel") || 
            Arrays.asList("Mahogany", "Teak", "Metal-Wood Hybrid", "Tempered Glass")
                  .contains(material)) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.5));
        }
        if (style.equals("Vintage") || style.equals("Art Deco")) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.3));
        }
        
        // Round to 2 decimal places
        basePrice = basePrice.setScale(2, RoundingMode.HALF_UP);
        
        door.setPrice(basePrice.doubleValue());
        door.setFinalPrice(basePrice.doubleValue()); // Will be calculated by entity
        door.setSeller(seller);
        door.setStatus(DoorStatus.AVAILABLE);
        door.setActive(true);
        
        return door;
    }
    
    /**
     * Copies door properties from source to target.
     * Like a door's identical twin, but with its own personality! 👯‍♂️
     */
    private void copyDoorProperties(Door source, Door target) {
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setSize(source.getSize());
        target.setCustomWidth(source.getCustomWidth());
        target.setCustomHeight(source.getCustomHeight());
        target.setDoorLocation(source.getDoorLocation());
        target.setFrameType(source.getFrameType());
        target.setHardware(source.getHardware());
        target.setMaterial(source.getMaterial());
        target.setManufacturer(source.getManufacturer());
        target.setWarrantyYears(source.getWarrantyYears());
        target.setPrice(source.getPrice());
        target.setFinalPrice(source.getFinalPrice());
        target.setSeller(source.getSeller());
        target.setStatus(source.getStatus());
        target.setActive(source.isActive());
        
        // Create new list for images
        List<String> images = new ArrayList<>(source.getImages());
        target.setImages(images);
    }
    
    /**
     * Generates a random hex color code.
     * For when standard colors just won't cut it! 🎨
     */
    private String generateRandomHexColor() {
        return String.format("#%06x", faker.number().numberBetween(0, 0x1000000));
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
        BigDecimal basePrice = switch (type) {
            case HANDLE -> BigDecimal.valueOf(faker.number().numberBetween(2999, 19999))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case LOCK -> BigDecimal.valueOf(faker.number().numberBetween(7999, 39999))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case HINGES -> BigDecimal.valueOf(faker.number().numberBetween(1999, 5999))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case AUTOMATIC_CLOSER -> BigDecimal.valueOf(faker.number().numberBetween(9999, 29999))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO;
        };
        
        // Add premium for special materials
        if (material.contains("Gold") || material.contains("Titanium")) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.5));
        }

        FurnitureDoor door = new FurnitureDoor();
        door.setName(prefix + " " + faker.commerce().productName());
        door.setMaterial(material);
        door.setDescription(generateDescription(type, material));
        door.setPrice(basePrice.doubleValue());
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

    /**
     * Initializes sample mouldings with various styles and materials.
     * Because every door needs a beautiful frame! 🖼️
     *
     * @param seller The seller who owns these mouldings
     */
    private void initializeSampleMouldings(User seller) {
        System.out.println("🎨 Creating beautiful mouldings...");
        List<Moulding> mouldings = new ArrayList<>();

        // Create 15-20 sample mouldings
        int mouldingCount = faker.number().numberBetween(15, 21);

        for (int i = 0; i < mouldingCount; i++) {
            Moulding moulding = new Moulding();
            
            // Set basic properties
            String style = MOULDING_STYLES[faker.number().numberBetween(0, MOULDING_STYLES.length)];
            String material = MOULDING_MATERIALS[faker.number().numberBetween(0, MOULDING_MATERIALS.length)];
            
            moulding.setTitle(style + " - " + material);
            moulding.setSize(MOULDING_SIZES[faker.number().numberBetween(0, MOULDING_SIZES.length)]);
            moulding.setArticle("M-" + String.format("%05d", faker.number().numberBetween(1, 99999)));
            
            // Set price between $10 and $200
            double price = 10 + (faker.number().randomDouble(2, 0, 190));
            moulding.setPrice(price);
            
            // Set quantity between 50 and 500
            int quantity = faker.number().numberBetween(50, 501);
            moulding.setQuantity(quantity);
            
            // Calculate total price
            moulding.setPriceOverall(price * quantity);
            
            // Set title and description
            moulding.setTitle(String.format("%s %s Moulding", material, style));
            moulding.setDescription(generateMouldingDescription(material, style));
            
            // Set seller
            moulding.setUser(seller);
            
            // Generate and set image URLs
            List<String> imageUrls = generateMouldingImages();
            moulding.setImagesUrl(imageUrls);
            
            mouldings.add(moulding);
        }
        
        // Save all mouldings
        mouldingRepository.saveAll(mouldings);
        System.out.println("✨ Created " + mouldingCount + " beautiful mouldings!");
    }

    /**
     * Generates a creative description for a moulding.
     * Because every moulding has a story to tell! 📖
     */
    private String generateMouldingDescription(String material, String style) {
        String[] features = {
            "Adds elegance to any room",
            "Perfect for traditional and modern spaces",
            "Easy to install",
            "Durable and long-lasting",
            "Excellent paint adhesion",
            "Resistant to warping",
            "Moisture-resistant",
            "Premium finish"
        };

        String mainDesc = String.format("Exquisite %s %s crafted from premium %s material. ", 
            style, material, material);

        // Add 2-3 random features
        Set<String> selectedFeatures = new HashSet<>();
        int featureCount = faker.number().numberBetween(2, 4);
        while (selectedFeatures.size() < featureCount) {
            selectedFeatures.add(features[faker.number().numberBetween(0, features.length)]);
        }

        return mainDesc + String.join(". ", selectedFeatures) + ".";
    }

    /**
     * Generates image URLs for mouldings using Unsplash IDs.
     * A moulding without pictures is like a door without a handle! 📸
     */
    private List<String> generateMouldingImages() {
        List<String> imageUrls = new ArrayList<>();
        int imageCount = faker.number().numberBetween(1, 4);
        
        for (int i = 0; i < imageCount; i++) {
            String imageId = MOULDING_IMAGE_IDS[faker.number().numberBetween(0, MOULDING_IMAGE_IDS.length)];
            imageUrls.add("https://images.unsplash.com/photo-" + imageId);
        }
        
        return imageUrls;
    }

    /**
     * Initialize sample store addresses.
     * Because every door needs a home! 🏠
     */
    private void initializeSampleAddresses() {
        System.out.println("📍 Setting up our door destinations...");
        
        for (String[] locationData : STORE_LOCATIONS) {
            // Create location first
            Location location = new Location();
            location.setLatitude(Double.parseDouble(locationData[6]));
            location.setLongitude(Double.parseDouble(locationData[7]));
            location.setMarkerTitle(locationData[2]); // Use store name as marker title
            
            // Create address
            Address address = new Address();
            address.setCity(locationData[0]);
            address.setStreet(locationData[1]);
            address.setName(locationData[2]);
            address.setWorkingHours(locationData[3]);
            address.setPhoneNumber(locationData[4]);
            address.setEmail(locationData[5]);
            address.setDefault(false);
            address.setLocation(location);
            
            // Set up some social links
            Map<Socials, String> socialLinks = new HashMap<>();
            socialLinks.put(Socials.FACEBOOK, "https://facebook.com/doorparadise" + locationData[0].toLowerCase().replace(" ", ""));
            socialLinks.put(Socials.INSTAGRAM, "https://instagram.com/doorparadise" + locationData[0].toLowerCase().replace(" ", ""));
            address.setSocialLinks(socialLinks);
            
            addressRepository.save(address);
        }
        
        System.out.println("🎯 Door destinations are ready for visitors!");
    }

    /**
     * Creates default door categories.
     * 
     * Every door needs a family! Here's where we sort them into their respective clans. 🏰
     */
    private void createDefaultCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                new Category(null, "Interior Doors", true),
                new Category(null, "Exterior Doors", true),
                new Category(null, "Security Doors", true),
                new Category(null, "Sliding Doors", true),
                new Category(null, "French Doors", true),
                new Category(null, "Barn Doors", true),
                new Category(null, "Smart Doors", true)
            );
            categoryRepository.saveAll(categories);
        }
    }

    /**
     * Creates a diverse set of default doors with varying attributes.
     * These doors are carefully crafted to demonstrate the getSimillarDoors functionality.
     * 
     * Warning: Side effects may include an overwhelming desire to renovate your house! 🏗️
     */
    private void createDefaultDoors() {
        if (doorRepository.count() == 0) {
            // Get categories
            Category interior = categoryRepository.findByName("Interior Doors").orElseThrow();
            Category exterior = categoryRepository.findByName("Exterior Doors").orElseThrow();
            Category security = categoryRepository.findByName("Security Doors").orElseThrow();
            Category sliding = categoryRepository.findByName("Sliding Doors").orElseThrow();
            Category french = categoryRepository.findByName("French Doors").orElseThrow();
            
            // Create a default seller if none exists
            User seller = userRepository.findByRole(Role.SELLER)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    User newSeller = new User();
                    newSeller.setName("Door Master");
                    newSeller.setEmail("seller@doors.com");
                    newSeller.setPassword(passwordEncoder.encode("password123"));
                    newSeller.setRole(Role.SELLER);
                    newSeller.setPhone("+1234567890");  // Adding required phone number
                    return userRepository.save(newSeller);
                });

            // Create doors with diverse attributes for similar door matching
            List<Door> doors = new ArrayList<>();

            // Classic Oak Interior Door Series
            for (DoorLocation location : Arrays.asList(DoorLocation.BEDROOM, DoorLocation.LIVING_ROOM, DoorLocation.BATHROOM)) {
                Door door = new Door();
                door.setName("Classic Oak " + location.name());
                door.setDescription("Timeless oak door perfect for your " + location.name().toLowerCase());
                door.setPrice(299.99);
                door.setFinalPrice(299.99);
                door.setCategory(interior);
                door.setImages(Arrays.asList(DOOR_IMAGE_IDS[0]));
                door.setSize(Size.SIZE_800x2000);  // Standard interior door size
                door.setColor(Color.GOLDEN_OAK);  // Warm golden oak color for classic doors
                door.setMaterial("Solid Oak");
                door.setManufacturer("DoorMaster Pro");
                door.setWarrantyYears(5);
                door.setStatus(DoorStatus.AVAILABLE);
                door.setActive(true);
                door.setSeller(seller);
                door.setDoorLocation(location);
                door.setFrameType(FrameType.STANDARD);  // Standard frame for interior doors
                door.setHardware(HardwareType.STANDARD_HINGES);  // Basic hinges for interior doors
                doors.add(door);
            }

            // Modern Security Series
            for (Color color : Arrays.asList(Color.BLACK, Color.CHARCOAL, Color.GRAY)) {
                Door door = new Door();
                door.setName("Modern Security " + color.name());
                door.setDescription("High-security door with modern aesthetics");
                door.setPrice(899.99);
                door.setFinalPrice(899.99);
                door.setCategory(security);
                door.setImages(List.of(DOOR_IMAGE_IDS[1]));
                door.setSize(Size.SIZE_900x2000);  // Larger security door size
                door.setColor(color);  // Using the color parameter (BLACK, CHARCOAL, GRAY)
                door.setMaterial("Reinforced Steel");
                door.setManufacturer("SecureMax Pro");
                door.setWarrantyYears(10);
                door.setStatus(DoorStatus.AVAILABLE);
                door.setActive(true);
                door.setSeller(seller);
                door.setDoorLocation(DoorLocation.ENTRANCE);
                door.setFrameType(FrameType.REBATED);  // Rebated frame for better security
                door.setHardware(HardwareType.AUTOMATIC);  // Smart automatic hardware
                doors.add(door);
            }

            // Elegant French Doors
            for (String material : Arrays.asList("Mahogany", "Cherry Wood", "Walnut")) {
                Door door = new Door();
                door.setName("Elegant " + material + " French Door");
                door.setDescription("Sophisticated French door crafted from premium " + material);
                door.setPrice(1299.99);
                door.setFinalPrice(1299.99);
                door.setCategory(french);
                door.setImages(Arrays.asList(DOOR_IMAGE_IDS[2]));
                door.setSize(Size.SIZE_1200x2000);  // Double door size for French doors
                door.setColor(Color.MAHOGANY);  // Rich mahogany color for elegant doors
                door.setMaterial(material);
                door.setManufacturer("Portal Paradise");
                door.setWarrantyYears(7);
                door.setStatus(DoorStatus.AVAILABLE);
                door.setActive(true);
                door.setSeller(seller);
                door.setDoorLocation(DoorLocation.LIVING_ROOM);
                door.setFrameType(FrameType.NON_REBATED);  // Non-rebated for elegant look
                door.setHardware(HardwareType.CONCEALED_HINGES);  // Hidden hinges for elegance
                doors.add(door);
            }

            // Modern Sliding Series
            for (FrameType frameType : Arrays.asList(FrameType.REBATED, FrameType.NON_REBATED, FrameType.TELESCOPIC)) {
                Door door = new Door();
                door.setName("Modern " + frameType.name() + " Slider");
                door.setDescription("Contemporary sliding door with " + frameType.name().toLowerCase() + " frame");
                door.setPrice(799.99);
                door.setFinalPrice(799.99);
                door.setCategory(sliding);
                door.setImages(Arrays.asList(DOOR_IMAGE_IDS[3]));
                door.setSize(Size.CUSTOM);  // Custom size for sliding doors
                door.setColor(Color.GRAY);  // Modern gray for sliding doors
                door.setMaterial("Tempered Glass");
                door.setManufacturer("Swing Kings");
                door.setWarrantyYears(5);
                door.setStatus(DoorStatus.AVAILABLE);
                door.setActive(true);
                door.setSeller(seller);
                door.setDoorLocation(DoorLocation.BALCONY);
                door.setFrameType(frameType);  // Using the frameType parameter from the loop
                door.setHardware(HardwareType.SLIDING);  // Sliding hardware for sliding doors
                doors.add(door);
            }

            // Premium Exterior Series
            for (HardwareType hardware : Arrays.asList(HardwareType.AUTOMATIC, HardwareType.SPRING_HINGES, HardwareType.DOUBLE_ACTION)) {
                Door door = new Door();
                door.setName("Premium Exterior with " + hardware.getDisplayName());
                door.setDescription("High-end exterior door with " + hardware.getDisplayName().toLowerCase() + " hardware");
                door.setPrice(1499.99);
                door.setFinalPrice(1499.99);
                door.setCategory(exterior);
                door.setImages(Arrays.asList(DOOR_IMAGE_IDS[4]));
                door.setSize(Size.SIZE_1000x2000);  // Large exterior door size
                door.setColor(Color.WHITE);  // Classic white for exterior doors
                door.setMaterial("Fiberglass");
                door.setManufacturer("Gateway Giants");
                door.setWarrantyYears(15);
                door.setStatus(DoorStatus.AVAILABLE);
                door.setActive(true);
                door.setSeller(seller);
                door.setDoorLocation(DoorLocation.ENTRANCE);
                door.setFrameType(FrameType.TELESCOPIC);  // Telescopic frame for exterior doors
                door.setHardware(hardware);
                doors.add(door);
            }

            doorRepository.saveAll(doors);
        }
    }
}
