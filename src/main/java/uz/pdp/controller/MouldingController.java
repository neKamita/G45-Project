package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.dto.MouldingDTO;
import uz.pdp.entity.Moulding;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.MouldingService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing moulding operations.
 * Handles CRUD operations for mouldings, with role-based access control.
 */
@RestController
@RequestMapping("/api/mouldings")
@Tag(name = "Moulding Management", description = "APIs for managing mouldings")
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
        

    // Delete a Moulding by ID (Accessible by Admin and Seller)


    // Upload an image for a Moulding
    @PostMapping("/upload-attachment")
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

