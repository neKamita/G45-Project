package uz.pdp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;  // Update import
import uz.pdp.repository.DoorRepository;

@Service
public class DoorSecurityService {
    @Autowired
    private DoorRepository doorRepository;
    
    @Autowired
    private UserService userService;

    public boolean isSeller(Long doorId) {
        Door door = doorRepository.findById(doorId)
            .orElseThrow(() -> new EntityNotFoundException("Door not found"));
        User currentUser = userService.getCurrentUser();
        return door.getSeller().getId().equals(currentUser.getId());
    }
}