package br.com.fiap.Phycocarbon.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TanqueDTOs {

    public record CriarTanqueRequest(
            @NotNull(message = "ID da fazenda é obrigatório")
            Long idFazenda,

            @NotBlank(message = "Código do tanque é obrigatório")
            @Size(max = 30)
            String codigoTanque,

            @NotBlank(message = "Tipo de alga é obrigatório")
            String tipoAlga,

            @Positive(message = "Capacidade deve ser positiva")
            BigDecimal capacidadeLitros,

            @DecimalMin("0.0") @DecimalMax("14.0")
            BigDecimal phMin,

            @DecimalMin("0.0") @DecimalMax("14.0")
            BigDecimal phMax,

            BigDecimal temperaturaMin,
            BigDecimal temperaturaMax,
            LocalDate dtInstalacao
    ) {}

    public record AtualizarTanqueRequest(
            String tipoAlga,
            BigDecimal capacidadeLitros,
            BigDecimal phMin,
            BigDecimal phMax,
            BigDecimal temperaturaMin,
            BigDecimal temperaturaMax,
            String status
    ) {}
}
