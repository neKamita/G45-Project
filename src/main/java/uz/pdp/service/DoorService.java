package uz.pdp.service;

import java.util.List;

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

    @Autowired
    private DoorRepository doorRepository;

    @Cacheable(value = "doors", key = "#id")
    public Door getDoor(Long id) {
        return doorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Door not found with id: " + id));
    }
    
    @Cacheable(value = "allDoors")
    public List<Door> getAllDoors() {
        return doorRepository.findAll();
    }

    @CachePut(value = "doors", key = "#door.id")
    public Door createDoor(Door door) {
        return doorRepository.save(door);
    }

    @CachePut(value = "doors", key = "#id")
    public Door updateDoor(Long id, Door updatedDoor) {
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
        return doorRepository.save(door);
    }

    @CacheEvict(value = {"doors", "allDoors"}, allEntries = true)
    public void deleteDoor(Long id) {
        Door door = getDoor(id);
        doorRepository.delete(door);
    }

    public Door configureDoor(Long id, Size size, Color color, Double width, Double height) {
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
        
        return doorRepository.save(door);
    }
}