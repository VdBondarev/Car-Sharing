package car.sharing.repository.specification.car;

import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.model.Car;
import car.sharing.repository.specification.SpecificationBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarSpecificationBuilder
        implements SpecificationBuilder<Car, CarSearchParametersDto> {
    private static final String BRAND_COLUMN = "brand";
    private static final String MODEL_COLUMN = "model";
    private final CarInSpecificationProviderManager inSpecificationProviderManager;
    private final TypeSpecificationProvider typeSpecificationProvider;
    private final DailyFeeSpecificationProvider priceSpecificationProvider;

    @Override
    public Specification<Car> build(CarSearchParametersDto parametersDto) {
        Specification<Car> specification = Specification.where(null);
        if (notEmpty(parametersDto.models())) {
            specification = getInSpecification(
                    MODEL_COLUMN, specification, parametersDto.models());
        }
        if (notEmpty(parametersDto.brands())) {
            specification = getInSpecification(
                    BRAND_COLUMN, specification, parametersDto.brands());
        }
        if (parametersDto.priceBetween() != null) {
            specification = specification.and(
                    priceSpecificationProvider
                            .getSpecification(parametersDto.priceBetween()));
        }
        if (parametersDto.types() != null && !parametersDto.types().isEmpty()) {
            specification = specification.and(
                    typeSpecificationProvider
                            .getSpecification(
                                    parametersDto.types()));
        }
        return specification;
    }

    private Specification<Car> getInSpecification(
            String column,
            Specification<Car> specification,
            List<String> params) {
        return specification.and(
                inSpecificationProviderManager
                        .getSpecificationProvider(column)
                        .getSpecification(params));
    }

    private boolean notEmpty(List<String> params) {
        return params != null && !params.isEmpty();
    }
}
