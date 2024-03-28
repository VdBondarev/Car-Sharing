package car.sharing.service.payment;

import car.sharing.dto.payment.PaymentResponseDto;
import car.sharing.model.User;
import com.stripe.exception.StripeException;
import java.net.MalformedURLException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    List<PaymentResponseDto> getUserPayments(Long userId, Pageable pageable);

    PaymentResponseDto create(User user)
            throws StripeException, MalformedURLException;

    PaymentResponseDto success(User user);

    void cancel(User user);

    PaymentResponseDto getMyPayment(User user);
}
