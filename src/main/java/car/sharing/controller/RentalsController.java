package car.sharing.controller;

import car.sharing.dto.rental.RentalResponseDto;
import car.sharing.model.User;
import car.sharing.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rentals controller", description = "Endpoints for managing rentals")
@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalsController {
    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a rental",
            description = "Endpoint for creating a new rental")
    public RentalResponseDto addRental(
            Authentication authentication,
            @RequestParam(name = "car_id") Long carId,
            @RequestParam(name = "days_to_rent") @Min(0) int daysToRent) {
        return rentalService.addRental(
                getUser(authentication),
                carId,
                daysToRent);
    }

    @PutMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a pending rental",
            description = "Endpoint for cancelling the pending rental")
    public void cancel(Authentication authentication) {
        rentalService.cancel(getUser(authentication));
    }

    @GetMapping("/mine")
    @Operation(summary = "See all your rentals",
            description = "Endpoint seeing all your rentals with pageable sorting")
    public List<RentalResponseDto> getAllRentals(
            Authentication authentication,
            Pageable pageable) {
        return rentalService.getAllRentals(
                getUser(authentication),
                pageable);
    }

    @PostMapping("/return")
    @Operation(summary = "Set an actual return date",
            description = "Endpoint for setting a the return date of the car. "
                    + "If car is returned later than expected, then user should pay fine")
    public RentalResponseDto setReturnDate(
            Authentication authentication) {
        return rentalService.setReturnDate(getUser(authentication));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Get all user's rentals",
            description = "Endpoint for getting all user's rentals with pageable sorting."
                    + " Allowed for Managers only")
    public List<RentalResponseDto> getUserRentals(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "is_active") boolean isActive,
            Pageable pageable) {
        return rentalService.getUserRentals(userId, isActive, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Get a specific rental",
            description = "Endpoint for getting a specific rental."
                    + " Allowed for Managers only")
    public RentalResponseDto getSpecificRental(
            @PathVariable Long id) {
        return rentalService.getSpecificRental(id);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Get all active rentals",
            description = "Endpoint for getting all active rentals with pageable sorting."
                    + " Allowed for Managers only")
    public List<RentalResponseDto> getAllActive(Pageable pageable) {
        return rentalService.getAllActive(pageable);
    }

    private User getUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
