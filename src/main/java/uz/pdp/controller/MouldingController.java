package uz.pdp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.dto.MouldingDTO;
import uz.pdp.entity.Moulding;
import uz.pdp.service.MouldingService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/mouldings")
public class MouldingController {
    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private MouldingService mouldingService;
    // Get all Moulding items (Accessible by all users)
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    public List<MouldingDTO> getAllMouldings() {
        return mouldingService.getAllMouldings();
    }
    // Get a Moulding by ID (Accessible by all users)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    public ResponseEntity<MouldingDTO> getMouldingById(@PathVariable Long id) {
        Optional<MouldingDTO> moulding = mouldingService.getMouldingById(id);
        return moulding.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Add a new Moulding (Accessible by Admin and Seller)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<MouldingDTO> createMoulding(@RequestBody MouldingDTO mouldingDTO) {
        MouldingDTO createdMoulding = mouldingService.saveMoulding(mouldingDTO);
        return new ResponseEntity<>(createdMoulding, HttpStatus.CREATED);
    }

    // Update an existing Moulding (Accessible by Admin and Seller)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<MouldingDTO> updateMoulding(@PathVariable Long id, @RequestBody MouldingDTO mouldingDetails) {
        Optional<MouldingDTO> moulding = mouldingService.getMouldingById(id);
        
        if (moulding.isPresent()) {
            MouldingDTO updatedMoulding = moulding.get();
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

    // Delete a Moulding by ID (Accessible by Admin and Seller)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<Void> deleteMoulding(@PathVariable Long id) {
        mouldingService.deleteMoulding(id);
        return ResponseEntity.noContent().build();
    }
    // Upload an image for a Moulding
    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            return ResponseEntity.ok("Image uploaded successfully: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not upload file: " + e.getMessage());
        }
    }
}




