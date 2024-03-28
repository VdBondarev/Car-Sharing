package car.sharing.service.payment.strategy;

import car.sharing.model.Payment;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class InTimePaymentService implements PaymentService {
    private static final long DAYS_TO_ADD = 1L;

    @Override
    public BigDecimal calculateAmount(BigDecimal dailyFee, long days) {
        /*
        We add 1 to days for proper counting the money to pay
        For example, if user rents a car today and should return today
        Then days are 0, and user will pay nothing
        But with adding 1, user will pay properly like for 1 day
         */
        return dailyFee.multiply(
                BigDecimal.valueOf(days + DAYS_TO_ADD)
        ).multiply(BigDecimal.valueOf(100));
    }

    @Override
    public boolean isApplicable(Payment.Type type) {
        return type.equals(Payment.Type.PAYMENT);
    }
}
