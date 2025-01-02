package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Door getDoor(Long id) {
        return doorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Door not found with id: " + id));
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