package uz.pdp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.dto.MouldingDTO;
import uz.pdp.entity.Moulding;
import uz.pdp.repository.MouldingRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MouldingService {
    @Autowired
    private MouldingRepository mouldingRepository;

    // Get all Moulding items
    public List<MouldingDTO> getAllMouldings() {
        return mouldingRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get a specific Moulding by ID
    public Optional<MouldingDTO> getMouldingById(Long id) {
        return mouldingRepository.findById(id).map(this::convertToDTO);
    }

    // Save or update a Moulding
    public MouldingDTO saveMoulding(MouldingDTO mouldingDTO) {
        Moulding moulding = convertToEntity(mouldingDTO);
        if (moulding.getPrice() != null && moulding.getQuantity() != null) {
            moulding.setPriceOverall(moulding.getPrice() * moulding.getQuantity()); // Calculate total price
        } else {
            moulding.setPriceOverall(0.0); // Default value if price or quantity is null
        }
        return convertToDTO(mouldingRepository.save(moulding));
    }

    // Delete a Moulding by ID
    public void deleteMoulding(Long id) {
        mouldingRepository.deleteById(id);
    }

}
