package uz.pdp.service;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.repository.DoorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DoorService {

    @Autowired
    private DoorRepository doorRepository;

    Logger logger = LoggerFactory.getLogger(DoorService.class);

    @Transactional(readOnly = true)
    public Door getDoor(Long id) {
        return doorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Door not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Door> getAllDoors() {
        return doorRepository.findAll();
    }

    @Transactional
    @CachePut(key = "#door.id")
    public Door createDoor(Door door) {
        logger.info("Creating a new door: {}", door);
        if (door.getImages() == null) {
            door.setImages(new ArrayList<>());
        }
        if (door.getPrice() == null) {
            door.setPrice(0.0);
        }
        door.calculateFinalPrice();
        Door savedDoor = doorRepository.saveAndFlush(door);
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

    @Transactional(rollbackFor = Exception.class)
    @CachePut(key = "#id")
    @SneakyThrows
    public Door configureDoor(Long id, Size size, Color color, Double width, Double height) {
        logger.info("Configuring door with ID: {}, size: {}, color: {}, width: {}, height: {}", 
            id, size, color, width, height);
        Door door = getDoor(id);
        
        if (size != null) {
            door.setSize(size);
            if (size == Size.CUSTOM) {
                if (width == null || height == null) {
                    throw new Exception(
                        "Custom size requires both width and height to be specified");
                }
                door.setCustomWidth(width);
                door.setCustomHeight(height);
            }
        }
        
        if (color != null) {
            door.setColor(color);
            door.setIsCustomColor(color == Color.CUSTOM);
        }
        
        Door savedDoor = doorRepository.save(door);
        logger.info("Door with ID {} configured successfully", id);
        return savedDoor;
    }
}