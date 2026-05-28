package br.com.fiap.aquaorbital.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class MarketplaceDTOs {

    public record CriarLoteRequest(
            @NotNull Long idFazenda,
            @NotNull Long idTanque,
            @NotBlank String taxonomiaAlga,
            @NotNull @Positive BigDecimal pesoKg,
            @NotNull @Positive BigDecimal precoUnitario
    ) {}

    public record AtualizarStatusLoteRequest(@NotBlank String status) {}

    public record CriarTransacaoRequest(
            @NotNull Long idLote,
            Long idCredito,
            @NotBlank String tipoTransacao,
            @NotNull @Positive BigDecimal quantidade,
            @NotNull @Positive BigDecimal valorTotal
    ) {}
}