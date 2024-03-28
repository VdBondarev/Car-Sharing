package car.sharing.service.payment.strategy;

import car.sharing.model.Payment;
import java.math.BigDecimal;

public interface PaymentService {
    BigDecimal calculateAmount(BigDecimal dailyFee, long days);

    boolean isApplicable(Payment.Type type);
}
