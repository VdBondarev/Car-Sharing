package car.sharing.controller;

import car.sharing.dto.payment.PaymentResponseDto;
import car.sharing.model.User;
import car.sharing.service.payment.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.MalformedURLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment controller", description = "Endpoints for managing payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentsController {
    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a payment",
            description = "Endpoint for creating a payment for a pending rental")
    public PaymentResponseDto create(Authentication authentication)
            throws StripeException, MalformedURLException {
        return paymentService.create(getUser(authentication));
    }

    @GetMapping("/success")
    @Operation(summary = "Redirection after a successful payment",
            description = "Endpoint for redirection after the successful payment")
    public PaymentResponseDto success(Authentication authentication) {
        return paymentService.success(getUser(authentication));
    }

    @GetMapping("/cancel")
    @Operation(summary = "Redirection after a cancelled payment",
            description = "Endpoint for redirection after the cancelled payment")
    public void cancel(Authentication authentication) {
        paymentService.cancel(getUser(authentication));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get a pending payment",
            description = "Endpoint for getting the payment that have to be paid")
    public PaymentResponseDto getMyPendingPayment(
            Authentication authentication) {
        return paymentService.getMyPayment(getUser(authentication));
    }

    @GetMapping("/mine")
    @Operation(summary = "Get all your payments",
            description = "Endpoint for getting all your payments with pageable sorting")
    public List<PaymentResponseDto> getMyPayments(
            Authentication authentication,
            Pageable pageable) {
        return paymentService.getUserPayments(
                getUser(authentication).getId(),
                pageable);
    }

    @Operation(summary = "Get user's payments",
            description = "Endpoint for getting pointed user's payments."
                    + " Allowed for managers only")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping
    public List<PaymentResponseDto> getUserPayments(
            @RequestParam(name = "user_id") Long userId,
            Pageable pageable) {
        return paymentService.getUserPayments(userId, pageable);
    }

    private User getUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
