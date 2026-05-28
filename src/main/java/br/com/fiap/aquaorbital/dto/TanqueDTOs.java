package br.com.fiap.aquaorbital.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TanqueDTOs {

    public record CriarTanqueRequest(
            @NotNull Long idFazenda,
            @NotBlank @Size(max = 30) String codigoTanque,
            @NotBlank String tipoAlga,
            @Positive BigDecimal capacidadeLitros,
            @DecimalMin("0.0") @DecimalMax("14.0") BigDecimal phMin,
            @DecimalMin("0.0") @DecimalMax("14.0") BigDecimal phMax,
            BigDecimal temperaturaMin,
            BigDecimal temperaturaMax,
            LocalDate dtInstalacao
    ) {}

    public record AtualizarTanqueRequest(
            String tipoAlga, BigDecimal capacidadeLitros,
            BigDecimal phMin, BigDecimal phMax,
            BigDecimal temperaturaMin, BigDecimal temperaturaMax, String status
    ) {}
}