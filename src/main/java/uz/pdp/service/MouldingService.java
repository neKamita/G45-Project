package uz.pdp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.entity.Moulding;
import uz.pdp.repository.MouldingRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MouldingService {

    @Autowired
    private MouldingRepository mouldingRepository;
    public List<Moulding> getAllMouldings() {
        return mouldingRepository.findAll();
    }
    public Optional<Moulding> getMouldingById(Long id) {
        return mouldingRepository.findById(id);
    }
    public Moulding saveMoulding(Moulding moulding) {
        if (moulding.getPrice() != null && moulding.getQuantity() != null) {
            moulding.setPriceOverall(moulding.getPrice() * moulding.getQuantity()); // Calculate total price
        } else {
            moulding.setPriceOverall(0.0);
        }
        return mouldingRepository.save(moulding);
    }

    public void deleteMoulding(Long id) {
        mouldingRepository.deleteById(id);
    }
}
