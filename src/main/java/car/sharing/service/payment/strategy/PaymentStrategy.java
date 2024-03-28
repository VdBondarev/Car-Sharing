package car.sharing.service.payment.strategy;

import car.sharing.model.Payment;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentStrategy {
    private final List<PaymentService> paymentServices;

    public PaymentService getPaymentService(Payment.Type type) {
        return paymentServices
                .stream()
                .filter(paymentService ->
                        paymentService.isApplicable(type))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find payment service by payment type " + type));
    }
}
