package br.com.fiap.aquaorbital.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class FazendaDTOs {

    public record CriarFazendaRequest(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank String cidade,
            @NotBlank @Size(min = 2, max = 2) String uf,
            @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
    ) {}

    public record AtualizarFazendaRequest(
            @Size(max = 150) String nome,
            String cidade,
            @Size(min = 2, max = 2) String uf,
            BigDecimal latitude,
            BigDecimal longitude,
            String status
    ) {}
}