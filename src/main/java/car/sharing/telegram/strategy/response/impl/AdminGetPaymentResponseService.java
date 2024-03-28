package car.sharing.telegram.strategy.response.impl;

import car.sharing.model.Payment;
import car.sharing.repository.PaymentRepository;
import car.sharing.telegram.strategy.response.AdminResponseService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminGetPaymentResponseService implements AdminResponseService {
    private static final String PAYMENT_REGEX =
            "^(?i)Get info about a payment with id:\\s*\\d+$";
    private final PaymentRepository paymentRepository;

    @Override
    public String getMessage(String text) {
        Long paymentId = getId(text);
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        if (payment.isEmpty()) {
            return String.format("There is no payment by id %s", paymentId);
        }
        String message = """
                ***
                Found this payment.
                
                User's id: %s,
                Rental's id: %s,
                Status: %s,
                Type: %s,
                Amount to pay: %s
                ***
                """;
        return String.format(message,
                payment.get().getUserId(),
                payment.get().getRentalId(),
                payment.get().getStatus(),
                payment.get().getType(),
                payment.get().getAmountToPay());
    }

    @Override
    public boolean isApplicable(String text) {
        return text.matches(PAYMENT_REGEX);
    }
}
