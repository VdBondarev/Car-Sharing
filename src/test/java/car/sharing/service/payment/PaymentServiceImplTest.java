package car.sharing.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import car.sharing.dto.payment.PaymentResponseDto;
import car.sharing.mapper.PaymentMapper;
import car.sharing.model.Payment;
import car.sharing.model.User;
import car.sharing.repository.PaymentRepository;
import com.stripe.Stripe;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    private static final String SESSION_URL_EXAMPLE =
            "https://checkout.stripe.com/c/pay/"
                    + "cs_test_a143DWKOtHJiHXu9Tga1ZC644WC6roLAN0IuMSxwkpykVDYGR9oDEerQbP"
                    + "#fidkdWxOYHwnPyd1blpxYHZxWjA0SnB8NjQ3SX1NYGFRdW1fa1xCVTU0UXVCUGIwd2RiNE"
                    + "dqNzJuQk9nR2Exb31NXTdBV1NtTnJsNXc3N2E9al1GQWR2XHRtfFxHaF9WNDVHQkpvUTVBclFQN"
                    + "TVXUmM9ZjZWYScpJ2N3amhWYHdzYHcnP3F3cGApJ2lkf"
                    + "GpwcVF8dWAnPyd2bGtiaWBabHFgaCcpJ2BrZGdpYFVpZGZg"
                    + "bWppYWB3dic%2FcXdwYHgl";
    private static final String SESSION_ID_EXAMPLE =
            "cs_test_a143DWKOtHJiHXu9Tga1ZC644WC6roLAN0IuMSxwkpykVDYGR9oDEerQbP";
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeAll
    static void beforeAll() {
        Stripe.apiKey = "pk_test_51Ouy312LxHedTphZnYGP01TpGUg5rag"
                + "1Bo27kGJbBd4jxHX2DRVhKwi0r22d8oXCDasYqhyYBmZS10BGOjT0DwTU00RWf8c3Sd";
    }

    @Test
    @DisplayName("Verify that getUserPayments() method works as expected with valid params")
    void getUserPayments_ValidParams_ReturnsValidResponse()
            throws MalformedURLException {
        Pageable pageable = PageRequest.of(0, 20);

        Payment firstPayment = createPayment(Payment.Status.PAID, 1L, BigDecimal.valueOf(10 + 1));
        Payment secondPayment = createPayment(Payment.Status.PAID, 2L, BigDecimal.valueOf(10 + 2));

        List<Payment> expectedPayments = List.of(firstPayment, secondPayment);

        PaymentResponseDto firstDto = createResponseDto(firstPayment);
        PaymentResponseDto secondDto = createResponseDto(secondPayment);

        when(paymentRepository.findAllByUserId(1L, pageable)).thenReturn(expectedPayments);
        when(paymentMapper.toResponseDto(firstPayment)).thenReturn(firstDto);
        when(paymentMapper.toResponseDto(secondPayment)).thenReturn(secondDto);

        List<PaymentResponseDto> expected = List.of(firstDto, secondDto);
        List<PaymentResponseDto> actual = paymentService.getUserPayments(1L, pageable);

        assertEquals(expected, actual);

        verify(paymentRepository, times(1)).findAllByUserId(any(), any());
        verify(paymentMapper, times(2)).toResponseDto(any());
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify that getMyPayment() method works as expected with valid params")
    void getMyPayment_ValidParams_ReturnsValidResponse()
            throws MalformedURLException {
        Payment payment = createPayment(Payment.Status.PENDING, 1L, BigDecimal.TEN);

        PaymentResponseDto expected = createResponseDto(payment);

        User user = new User();
        user.setId(1L);

        when(paymentRepository.findByStatusAndUserId(Payment.Status.PENDING, user.getId()))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toResponseDto(any())).thenReturn(expected);

        PaymentResponseDto actual = paymentService.getMyPayment(user);

        assertEquals(expected, actual);
    }

    private PaymentResponseDto createResponseDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getRentalId(),
                payment.getStatus(),
                payment.getType(),
                payment.getSessionUrl(),
                payment.getSessionId(),
                payment.getAmountToPay());
    }

    private Payment createPayment(Payment.Status status, long rentalId, BigDecimal price)
            throws MalformedURLException {
        return new Payment.Builder()
                .setSessionUrl(new URL(SESSION_URL_EXAMPLE))
                .setSessionId(SESSION_ID_EXAMPLE)
                .setStatus(status)
                .setType(Payment.Type.PAYMENT)
                .setUserId(1L)
                .setRentalId(rentalId)
                .build();
    }

    @Test
    @DisplayName("Verify that getMyPayment() method works as expected with non-valid params")
    void getMyPayment_NonValidParams_ThrowsException() {
        User user = new User();
        user.setId(1L);

        when(paymentRepository.findByStatusAndUserId(any(), any())).thenReturn(Optional.empty());

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class,
                        () -> paymentService.getMyPayment(user));

        String expected = "You don't have an active payment";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }
}
