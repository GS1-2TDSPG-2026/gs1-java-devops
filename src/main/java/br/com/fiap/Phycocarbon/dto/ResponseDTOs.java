package br.com.fiap.Phycocarbon.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResponseDTOs {

    public record TokenResponse(String token, String tipo, String email, String perfil) {}

    public record UsuarioResponse(Long id, String nome, String email, String telefone,
                                  String perfil, String status, LocalDateTime dtCriacao) {}

    public record FazendaResponse(Long id, String nome, String cidade, String uf,
                                  BigDecimal latitude, BigDecimal longitude, String status,
                                  LocalDateTime dtCadastro, Long idUsuarioResponsavel, String nomeResponsavel) {}

    public record TanqueResponse(Long id, Long idFazenda, String nomeFazenda, String codigoTanque,
                                 String tipoAlga, BigDecimal capacidadeLitros, BigDecimal phMin,
                                 BigDecimal phMax, BigDecimal temperaturaMin, BigDecimal temperaturaMax,
                                 String status, LocalDate dtInstalacao) {}

    public record DadoOrbitalResponse(
            Long id,
            Long idFazenda,
            String nomeFazenda,
            String fonte,
            LocalDate dtColeta,
            BigDecimal irradianciaParTot,
            BigDecimal nebulosidade,
            BigDecimal temperaturaAmbiente,
            BigDecimal latitude,
            BigDecimal longitude,
            LocalDateTime dtRegistro
    ) {}

    public record LoteBiomassaResponse(Long id, Long idFazenda, String nomeFazenda, Long idTanque,
                                       String codigoTanque, String taxonomiaAlga, BigDecimal pesoKg,
                                       BigDecimal precoUnitario, String status, LocalDate dtColheita) {}

    public record CreditoCarbonoResponse(Long id, Long idFazenda, String nomeFazenda, Long idLote,
                                         BigDecimal co2Toneladas, String hashAuditoria,
                                         String status, LocalDateTime dtValidacao) {}

    public record TransacaoResponse(Long id, Long idComprador, String nomeComprador, Long idLote,
                                    Long idCredito, String tipoTransacao, BigDecimal quantidade,
                                    BigDecimal valorTotal, String status, LocalDateTime dtTransacao) {}

    public record DashboardFazendaResponse(Long idFazenda, String nomeFazenda, int totalTanques,
                                           int tanquesAtivos, int lotesDisponiveis,
                                           int creditosDisponiveis, BigDecimal totalCo2Toneladas,
                                           DadoOrbitalResponse ultimoDadoOrbital) {}
}
