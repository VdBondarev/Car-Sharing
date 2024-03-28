package car.sharing.mapper;

import car.sharing.config.MapperConfig;
import car.sharing.dto.payment.PaymentResponseDto;
import car.sharing.model.Payment;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentResponseDto toResponseDto(Payment payment);
}
