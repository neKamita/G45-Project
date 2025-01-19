package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.exception.GlobalExceptionHandler.FurnitureDoorNotFoundException;
import uz.pdp.repository.FurnitureDoorRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing furniture doors.
 * The workshop where door magic happens! 🪚✨
 */
@Service
@RequiredArgsConstructor
public class FurnitureDoorService {

    private final FurnitureDoorRepository furnitureDoorRepository;

    /**
     * Creates a new furniture door.
     * Like a door factory, but more magical! 🏭✨
     */
    public FurnitureDoor create(FurnitureDoor furnitureDoor) {
        return furnitureDoorRepository.save(furnitureDoor);
    }

    /**
     * Gets all furniture doors.
     * Opening all the doors at once! 🚪🚪🚪
     */
    public List<FurnitureDoor> getAll() {
        return furnitureDoorRepository.findAll();
    }

    /**
     * Gets a furniture door by ID.
     * The door detective is on the case! 🔍
     */
    public Optional<FurnitureDoor> getById(Long id) {
        return furnitureDoorRepository.findById(id);
    }

    /**
     * Updates an existing furniture door.
     * Time for a door makeover! 💅
     */
    public FurnitureDoor update(Long id, FurnitureDoor furnitureDoor) {
        if (!furnitureDoorRepository.existsById(id)) {
            throw new FurnitureDoorNotFoundException(id);
        }
        furnitureDoor.setId(id);
        return furnitureDoorRepository.save(furnitureDoor);
    }

    /**
     * Deletes a furniture door.
     * Giving doors a graceful exit! 👋
     */
    public void delete(Long id) {
        if (!furnitureDoorRepository.existsById(id)) {
            throw new FurnitureDoorNotFoundException(id);
        }
        furnitureDoorRepository.deleteById(id);
    }
}
