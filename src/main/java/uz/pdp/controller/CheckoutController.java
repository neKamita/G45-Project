package uz.pdp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.dto.CheckoutDTO;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.CheckoutService;

/**
 * Controller for handling checkout operations.
 * The grand entrance where orders begin their journey! 🚪✨
 */
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;

    /**
     * Process a checkout request.
     * Every great door deserves a great home! 
     *
     * @param dto The checkout information
     * @return Response containing order confirmation
     */
    @PostMapping
    public ResponseEntity<EntityResponse<String>> checkout(@Valid @RequestBody CheckoutDTO dto) {
        return ResponseEntity.ok(checkoutService.processCheckout(dto));
    }
}
