package uz.pdp.config;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uz.pdp.entity.Door;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.entity.Moulding;
import uz.pdp.entity.User;
import uz.pdp.enums.*;
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
        "75*38*2100", "100*25*2400", "150*18*2700",
        "120*40*3000", "90*30*2400", "60*20*2100",
        "80*35*2700", "110*45*3000"
    };

    // Moulding image IDs from Unsplash
    private static final String[] MOULDING_IMAGE_IDS = {
        "1600585152220-90363fe7e115",  // Classic crown moulding
        "1600573472550-8d929c783b6d",  // Modern baseboard
        "1600571124505-01193eeb16cd",  // Decorative panel moulding
        "1600585152915-d9d4b702c6bd",  // Elegant chair rail
        "1600585153490-76fb20a32601"   // Contemporary casing
    };

    @Override
    public void run(String... args) {
        if (doorRepository.count() == 0 && furnitureDoorRepository.count() == 0 && mouldingRepository.count() == 0) {
            System.out.println("🎭 Welcome to the Door Paradise Initialization!");
            System.out.println("🏗️ Building your door empire...");
            
            // First, create our legendary door master
            User seller = createSampleSeller();
            System.out.println("👔 Door Master has entered the building!");
            
            // Create the main attractions - the doors!
            initializeSampleDoors(seller);
            
            // Now add some bling - door accessories
            initializeSampleFurnitureDoors();

            // Finally, add the finishing touches - mouldings!
            initializeSampleMouldings(seller);
            
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
}
