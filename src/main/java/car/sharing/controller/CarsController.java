package car.sharing.controller;

import car.sharing.dto.car.CarResponseDto;
import car.sharing.dto.car.CarSearchParametersDto;
import car.sharing.dto.car.CarUpdateDto;
import car.sharing.dto.car.CreateCarRequestDto;
import car.sharing.service.car.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars controller", description = "Endpoints for managing cars")
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarsController {
    private final CarService carService;

    @GetMapping
    @Operation(summary = "Get all available cars",
            description = "Endpoint for seeing all available cars with pageable sorting. "
                    + "Allowed for all user. Even those not authenticated")
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search cars by params",
            description = "Endpoint for searching cars by params with pageable sorting")
    public List<CarResponseDto> findByParams(
            @RequestBody @Valid CarSearchParametersDto parametersDto,
            Pageable pageable) {
        return carService.search(parametersDto, pageable);
    }

    @GetMapping ("/{id}")
    @Operation(summary = "Get the pointed car's info",
            description = "Endpoint for seeing info about a specific car")
    public CarResponseDto getInfo(@PathVariable Long id) {
        return carService.getInfo(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new car",
            description = "Endpoint for creating and saving a new car to database."
                    + " Allowed for managers only")
    public CarResponseDto create(
            @RequestBody @Valid CreateCarRequestDto requestDto) {
        return carService.create(requestDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update a car",
            description = "Endpoint for updating the pointed car."
                    + " Allowed for managers only")
    public CarResponseDto update(
            @PathVariable Long id,
            @RequestBody CarUpdateDto updateDto) {
        return carService.update(id, updateDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a new car",
            description = "Endpoint for deleting the pointed car from database."
                    + " Allowed for managers only")
    public void delete(@PathVariable Long id) {
        carService.delete(id);
    }
}
