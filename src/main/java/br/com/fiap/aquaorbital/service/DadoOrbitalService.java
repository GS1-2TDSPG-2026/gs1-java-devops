package br.com.fiap.aquaorbital.service;

import br.com.fiap.aquaorbital.dto.NasaPowerDTOs;
import br.com.fiap.aquaorbital.dto.ResponseDTOs.DadoOrbitalResponse;
import br.com.fiap.aquaorbital.entity.DadoOrbital;
import br.com.fiap.aquaorbital.entity.Fazenda;
import br.com.fiap.aquaorbital.repository.DadoOrbitalRepository;
import br.com.fiap.aquaorbital.repository.FazendaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DadoOrbitalService {

    private final DadoOrbitalRepository dadoOrbitalRepository;
    private final FazendaRepository fazendaRepository;
    private final RestTemplate restTemplate;

    private static final String NASA_POWER_URL = "https://power.larc.nasa.gov/api/temporal/daily/point";
    private static final String FONTE = "NASA_POWER";

    @Transactional
    public List<DadoOrbitalResponse> buscarEsalvarDadosNasa(Long fazendaId) {
        Fazenda fazenda = fazendaRepository.findById(fazendaId)
                .orElseThrow(() -> new EntityNotFoundException("Fazenda não encontrada: " + fazendaId));

        if (fazenda.getLatitude() == null || fazenda.getLongitude() == null) {
            throw new IllegalArgumentException("Fazenda não possui coordenadas geográficas cadastradas");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        String start = inicioMes.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String end = hoje.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = UriComponentsBuilder.fromUriString(NASA_POWER_URL) // <- alteração
                .queryParam("parameters", "ALLSKY_SFC_PAR_TOT,T2M,CLOUD_AMT")
                .queryParam("community", "AG")
                .queryParam("longitude", fazenda.getLongitude().toPlainString())
                .queryParam("latitude", fazenda.getLatitude().toPlainString())
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("format", "JSON")
                .toUriString();

        log.info("Chamando NASA POWER API para fazenda {} | URL: {}", fazendaId, url);

        NasaPowerDTOs nasaResponse = restTemplate.getForObject(url, NasaPowerDTOs.class);

        if (nasaResponse == null || nasaResponse.properties() == null) {
            throw new RuntimeException("NASA POWER API não retornou dados para as coordenadas informadas");
        }

        Map<String, Map<String, Double>> parametros = nasaResponse.properties().parameter();
        Map<String, Double> par = parametros.get("ALLSKY_SFC_PAR_TOT");
        Map<String, Double> temp = parametros.get("T2M");
        Map<String, Double> cloud = parametros.get("CLOUD_AMT");

        par.forEach((dataStr, valorPar) -> {
            LocalDate data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("yyyyMMdd"));

            Optional<DadoOrbital> existente = dadoOrbitalRepository.findByFazendaIdAndDtColeta(fazendaId, data);
            if (existente.isPresent()) return;

            Double valorTemp = temp != null ? temp.get(dataStr) : null;
            Double valorCloud = cloud != null ? cloud.get(dataStr) : null;

            if (valorPar == null || valorPar == -999.0) return;

            DadoOrbital dado = DadoOrbital.builder()
                    .fazenda(fazenda)
                    .fonte(FONTE)
                    .dtColeta(data)
                    .irradianciaParValue(BigDecimal.valueOf(valorPar))
                    .temperaturaAmbiente(valorTemp != null && valorTemp != -999.0
                            ? BigDecimal.valueOf(valorTemp) : null)
                    .nebulosidade(valorCloud != null && valorCloud != -999.0
                            ? BigDecimal.valueOf(valorCloud) : null)
                    .latitude(fazenda.getLatitude())
                    .longitude(fazenda.getLongitude())
                    .build();

            dadoOrbitalRepository.save(dado);
        });

        return dadoOrbitalRepository
                .findByFazendaIdOrderByDtColetaDesc(fazendaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DadoOrbitalResponse> listarPorFazenda(Long fazendaId) {
        if (!fazendaRepository.existsById(fazendaId)) {
            throw new EntityNotFoundException("Fazenda não encontrada: " + fazendaId);
        }
        return dadoOrbitalRepository
                .findByFazendaIdOrderByDtColetaDesc(fazendaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DadoOrbitalResponse> listarPorPeriodo(Long fazendaId, LocalDate inicio, LocalDate fim) {
        if (!fazendaRepository.existsById(fazendaId)) {
            throw new EntityNotFoundException("Fazenda não encontrada: " + fazendaId);
        }
        return dadoOrbitalRepository
                .findByFazendaIdAndDtColetaBetweenOrderByDtColetaDesc(fazendaId, inicio, fim)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DadoOrbitalResponse buscarUltimo(Long fazendaId) {
        return dadoOrbitalRepository
                .findTopByFazendaIdOrderByDtColetaDesc(fazendaId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum dado orbital encontrado para a fazenda: " + fazendaId));
    }

    public DadoOrbitalResponse toResponse(DadoOrbital d) {
        return new DadoOrbitalResponse(
                d.getId(),
                d.getFazenda().getId(),
                d.getFazenda().getNome(),
                d.getFonte(),
                d.getDtColeta(),
                d.getIrradianciaParValue(),
                d.getNebulosidade(),
                d.getTemperaturaAmbiente(),
                d.getLatitude(),
                d.getLongitude(),
                d.getDtRegistro()
        );
    }
}