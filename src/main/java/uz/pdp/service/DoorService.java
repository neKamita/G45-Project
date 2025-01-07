package uz.pdp.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.repository.DoorRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class DoorService {

    private static final Logger logger = LoggerFactory.getLogger(DoorService.class);

    @Autowired
    private DoorRepository doorRepository;

    @Cacheable(value = "doors", key = "#id")
    public Door getDoor(Long id) {
        logger.info("Fetching door with ID: {}", id);
        return doorRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Door not found with ID: {}", id);
                return new EntityNotFoundException("Door not found with id: " + id);
            });
    }
    
    @Cacheable(value = "allDoors")
    public List<Door> getAllDoors() {
        logger.info("Fetching all doors.");
        return doorRepository.findAll();
    }

    @CachePut(value = "doors", key = "#door.id")
    public Door createDoor(Door door) {
        logger.info("Creating a new door: {}", door);
        Door savedDoor = doorRepository.save(door);
        logger.info("Door created with ID: {}", savedDoor.getId());
        return savedDoor;
    }

    @CachePut(value = "doors", key = "#id")
    public Door updateDoor(Long id, Door updatedDoor) {
        logger.info("Updating door with ID: {}, data: {}", id, updatedDoor);
        Door door = getDoor(id);
        door.setName(updatedDoor.getName());
        door.setDescription(updatedDoor.getDescription());
        door.setPrice(updatedDoor.getPrice());
        door.setImages(updatedDoor.getImages());
        door.setSize(updatedDoor.getSize());
        door.setColor(updatedDoor.getColor());
        door.setMaterial(updatedDoor.getMaterial());
        door.setManufacturer(updatedDoor.getManufacturer());
        door.setWarrantyYears(updatedDoor.getWarrantyYears());
        door.setCustomWidth(updatedDoor.getCustomWidth());
        door.setCustomHeight(updatedDoor.getCustomHeight());
        door.setIsCustomColor(updatedDoor.getIsCustomColor());
        Door savedDoor = doorRepository.save(door);
        logger.info("Door with ID {} updated.", id);
        return savedDoor;
    }

    @CacheEvict(value = {"doors", "allDoors"}, allEntries = true)
    public void deleteDoor(Long id) {
        logger.info("Deleting door with ID: {}", id);
        Door door = getDoor(id);
        doorRepository.delete(door);
        logger.info("Door with ID {} deleted.", id);
    }

    public Door configureDoor(Long id, Size size, Color color, Double width, Double height) {
        logger.info("Configuring door with ID: {}, size: {}, color: {}, width: {}, height: {}", id, size, color, width, height);
        Door door = getDoor(id);
        
        if (size != null) {
            door.setSize(size);
            if (size == Size.CUSTOM && width != null && height != null) {
                door.setCustomWidth(width);
                door.setCustomHeight(height);
            }
        }
        
        if (color != null) {
            door.setColor(color);
            door.setIsCustomColor(color == Color.CUSTOM);
        }
        
        Door savedDoor = doorRepository.save(door);
        logger.info("Door with ID {} configured.", id);
        return savedDoor;
    }
}