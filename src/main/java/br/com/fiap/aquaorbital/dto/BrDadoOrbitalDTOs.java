package br.com.fiap.aquaorbital.dto;

import br.com.fiap.aquaorbital.entity.BrDadoOrbital;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BrDadoOrbitalDTOs(
        Long id,
        String codEstacao,
        String nomeEstacao,
        String dataMedicao,
        String horaMedicao,
        BigDecimal tempMaxima,
        BigDecimal tempMinima,
        BigDecimal tempMedia,
        BigDecimal umidadeRelativa,
        BigDecimal precipitacao,
        BigDecimal velocidadeVento,
        BigDecimal direcaoVento,
        BigDecimal pressaoAtm,
        BigDecimal radiacaoGlobal,
        LocalDateTime dtInclusao
) {
    public static BrDadoOrbitalDTOs from(BrDadoOrbital e) {
        return new BrDadoOrbitalDTOs(
                e.getId(),
                e.getCodEstacao(),
                e.getNomeEstacao(),
                e.getDataMedicao(),
                e.getHoraMedicao(),
                e.getTempMaxima(),
                e.getTempMinima(),
                e.getTempMedia(),
                e.getUmidadeRelativa(),
                e.getPrecipitacao(),
                e.getVelocidadeVento(),
                e.getDirecaoVento(),
                e.getPressaoAtm(),
                e.getRadiacaoGlobal(),
                e.getDtInclusao()
        );
    }
}