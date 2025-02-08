package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.CheckoutDTO;
import uz.pdp.dto.CheckoutItemsDTO;
import uz.pdp.dto.CheckoutHistoryDTO;
import uz.pdp.dto.PriceListRequestDto;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.CheckoutService;
import uz.pdp.service.PriceListService;

/**
 * Controller for additional operations like checkout and price lists.
 * 
 * Where the magic of buying doors happens!
 * And where you can get those sweet, sweet price lists!
 */
@RestController
@RequestMapping("/api/v1/additional")
@Tag(name = "Additional Operations", description = "Endpoints for checkout and price lists")
@RequiredArgsConstructor
public class AdditionalController {
    private final CheckoutService checkoutService;
    private final PriceListService priceListService;

    /**
     * Get checkout history for the current user.
     * Time to reminisce about all the doors that found their forever homes! 
     *
     * @return Response containing list of checkout history entries
     */
    @Operation(
        summary = "Get checkout history",
        description = "Retrieve the complete checkout history for the current user"
    )
    @GetMapping("/history")
    public EntityResponse<List<CheckoutHistoryDTO>> getCheckoutHistory() {
        return checkoutService.getCheckoutHistory();
    }

    @Operation(summary = "Process checkout", description = "Process checkout for items in the basket")
    @PostMapping("/checkout")
    public EntityResponse<?> processCheckout(@Valid @RequestBody CheckoutDTO checkoutDto) {
        checkoutService.processCheckout(checkoutDto);
        return EntityResponse.success(
                "Order processed successfully!  Your doors are on their way to their new home!",
                true
        );
    }

    @Operation(
        summary = "Get price list", 
        description = "Generate and send a price list for a specific item with all its variants"
    )
    @PostMapping("/price-list")
    public EntityResponse<?> getPriceList(@Valid @RequestBody PriceListRequestDto request) {
        priceListService.generateAndSendPriceList(request);
        return EntityResponse.success("Price list has been sent to your email!  Check your inbox for all the details!", true);
    }
}
