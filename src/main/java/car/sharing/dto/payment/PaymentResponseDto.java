package car.sharing.dto.payment;

import car.sharing.model.Payment;
import java.math.BigDecimal;
import java.net.URL;

public record PaymentResponseDto(
        Long id,
        Long rentalId,
        Payment.Status status,
        Payment.Type type,
        URL sessionUrl,
        String sessionId,
        BigDecimal amountToPay
) {
}
