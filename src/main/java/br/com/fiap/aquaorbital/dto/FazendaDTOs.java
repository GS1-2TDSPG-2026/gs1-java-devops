package br.com.fiap.aquaorbital.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class FazendaDTOs {

    public record CriarFazendaRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
            String nome,

            @NotBlank(message = "Cidade é obrigatória")
            String cidade,

            @NotBlank(message = "UF é obrigatória")
            @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
            String uf,

            @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
            BigDecimal latitude,

            @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
            BigDecimal longitude
    ) {}

    public record AtualizarFazendaRequest(
            @Size(max = 150)
            String nome,

            String cidade,

            @Size(min = 2, max = 2)
            String uf,

            BigDecimal latitude,
            BigDecimal longitude,
            String status
    ) {}
}
