package br.com.fiap.Phycocarbon.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class MarketplaceDTOs {

    public record CriarLoteRequest(
            @NotNull(message = "ID da fazenda é obrigatório")
            Long idFazenda,

            @NotNull(message = "ID do tanque é obrigatório")
            Long idTanque,

            @NotBlank(message = "Taxonomia da alga é obrigatória")
            String taxonomiaAlga,

            @NotNull @Positive(message = "Peso deve ser positivo")
            BigDecimal pesoKg,

            @NotNull @Positive(message = "Preço deve ser positivo")
            BigDecimal precoUnitario
    ) {}

    public record AtualizarStatusLoteRequest(
            @NotBlank(message = "Status é obrigatório")
            String status
    ) {}

    public record CriarTransacaoRequest(

            Long idLote,


            Long idCredito,

            @NotBlank(message = "Tipo de transação é obrigatório")
            String tipoTransacao,

            @NotNull @Positive
            BigDecimal quantidade,

            @NotNull @Positive
            BigDecimal valorTotal
    ) {}
}
