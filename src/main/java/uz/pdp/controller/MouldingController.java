package uz.pdp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.entity.Moulding;
import uz.pdp.service.MouldingService;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/mouldings")
public class MouldingController {

    @Autowired
    private MouldingService mouldingService;
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    public List<Moulding> getAllMouldings() {
        return mouldingService.getAllMouldings();
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    public ResponseEntity<Moulding> getMouldingById(@PathVariable Long id) {
        Optional<Moulding> moulding = mouldingService.getMouldingById(id);
        return moulding.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Moulding> createMoulding(@RequestBody Moulding moulding) {
        Moulding createdMoulding = mouldingService.saveMoulding(moulding);
        return new ResponseEntity<>(createdMoulding, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Moulding> updateMoulding(@PathVariable Long id, @RequestBody Moulding mouldingDetails) {
        Optional<Moulding> moulding = mouldingService.getMouldingById(id);
        if (moulding.isPresent()) {
            Moulding updatedMoulding = moulding.get();
            updatedMoulding.setName(mouldingDetails.getName());
            updatedMoulding.setSize(mouldingDetails.getSize());
            updatedMoulding.setArticle(mouldingDetails.getArticle());
            updatedMoulding.setPrice(mouldingDetails.getPrice());
            updatedMoulding.setQuantity(mouldingDetails.getQuantity());
            updatedMoulding.setTitle(mouldingDetails.getTitle());
            updatedMoulding.setDescription(mouldingDetails.getDescription());
            updatedMoulding.setImagesUrl(mouldingDetails.getImagesUrl());
            return ResponseEntity.ok(mouldingService.saveMoulding(updatedMoulding));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Void> deleteMoulding(@PathVariable Long id) {
        mouldingService.deleteMoulding(id);
        return ResponseEntity.noContent().build();
    }
}

