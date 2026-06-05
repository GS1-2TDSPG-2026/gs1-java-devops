package br.com.fiap.Phycocarbon.service;

import br.com.fiap.Phycocarbon.dto.OpenMeteoDTOs;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.DadoOrbitalResponse;
import br.com.fiap.Phycocarbon.entity.DadoOrbital;
import br.com.fiap.Phycocarbon.entity.Fazenda;
import br.com.fiap.Phycocarbon.repository.DadoOrbitalRepository;
import br.com.fiap.Phycocarbon.repository.FazendaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class OpenMeteoService {

    private final DadoOrbitalRepository dadoOrbitalRepository;
    private final FazendaRepository     fazendaRepository;
    private final RestTemplate          restTemplate;
    private final DadoOrbitalService    dadoOrbitalService; // reutiliza toResponse()



    private static final String OPEN_METEO_URL  = "https://api.open-meteo.com/v1/forecast";
    private static final String FONTE           = "OPEN_METEO";
    private static final String TIMEZONE        = "America/Sao_Paulo";


    private static final DateTimeFormatter METEO_DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


    @Transactional
    public List<DadoOrbitalResponse> buscarESalvarDadosOpenMeteo(Long fazendaId) {

        Fazenda fazenda = fazendaRepository.findById(fazendaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fazenda não encontrada: " + fazendaId));

        if (fazenda.getLatitude() == null || fazenda.getLongitude() == null) {
            throw new IllegalArgumentException(
                    "Fazenda não possui coordenadas geográficas cadastradas");
        }

        String url = buildUrl(fazenda);
        log.info("Chamando Open-Meteo API | fazenda={} | URL: {}", fazendaId, url);

        OpenMeteoDTOs response = restTemplate.getForObject(url, OpenMeteoDTOs.class);

        if (response == null || response.hourly() == null) {
            log.error("Open-Meteo retornou resposta nula. URL: {}", url);
            throw new RuntimeException(
                    "Open-Meteo API não retornou dados para as coordenadas informadas");
        }

        List<DadoOrbital> salvos = processarHourly(fazenda, response.hourly());
        log.info("Open-Meteo sync concluída | fazenda={} | {} dias processados.",
                fazendaId, salvos.size());

        return dadoOrbitalRepository
                .findByFazendaIdOrderByDtColetaDesc(fazendaId)
                .stream()
                .map(dadoOrbitalService::toResponse)
                .toList();
    }


    private String buildUrl(Fazenda fazenda) {
        return UriComponentsBuilder.fromUriString(OPEN_METEO_URL)
                .queryParam("latitude",  fazenda.getLatitude().toPlainString())
                .queryParam("longitude", fazenda.getLongitude().toPlainString())
                .queryParam("hourly",
                        "shortwave_radiation,cloud_cover,temperature_2m,relative_humidity_2m")
                .queryParam("forecast_days", 2)
                .queryParam("timezone", TIMEZONE)
                .toUriString();
    }


    private List<DadoOrbital> processarHourly(Fazenda fazenda,
                                              OpenMeteoDTOs.HourlyData hourly) {

        List<String>  times       = hourly.time();
        List<Double>  radiations  = hourly.shortwaveRadiation();
        List<Double>  clouds      = hourly.cloudCover();
        List<Double>  temps       = hourly.temperature2m();

        if (times == null || times.isEmpty()) {
            log.warn("Open-Meteo retornou lista 'time' vazia para fazenda={}",
                    fazenda.getId());
            return List.of();
        }


        record Acc(List<Double> rad, List<Double> cld, List<Double> tmp) {}
        java.util.Map<LocalDate, Acc> byDay = new java.util.LinkedHashMap<>();

        for (int i = 0; i < times.size(); i++) {
            LocalDateTime ldt;
            try {
                ldt = LocalDateTime.parse(times.get(i), METEO_DT_FMT);
            } catch (Exception e) {
                log.warn("Open-Meteo: timestamp inválido '{}', ignorando.", times.get(i));
                continue;
            }
            LocalDate day = ldt.toLocalDate();
            byDay.computeIfAbsent(day, d -> new Acc(
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

            Acc acc = byDay.get(day);
            if (radiations != null && i < radiations.size() && radiations.get(i) != null)
                acc.rad().add(radiations.get(i));
            if (clouds != null && i < clouds.size() && clouds.get(i) != null)
                acc.cld().add(clouds.get(i));
            if (temps != null && i < temps.size() && temps.get(i) != null)
                acc.tmp().add(temps.get(i));
        }

        List<DadoOrbital> result = new ArrayList<>();

        byDay.forEach((day, acc) -> {

            BigDecimal avgRad  = average(acc.rad());
            BigDecimal avgCld  = average(acc.cld());
            BigDecimal avgTmp  = average(acc.tmp());

            // Upsert: atualiza se já existe OPEN_METEO para este dia/fazenda
            Optional<DadoOrbital> existente =
                    dadoOrbitalRepository.findByFazendaIdAndDtColeta(fazenda.getId(), day);

            DadoOrbital dado;
            if (existente.isPresent() && FONTE.equals(existente.get().getFonte())) {

                dado = existente.get();
                dado.setIrradianciaParTot(avgRad);
                dado.setNebulosidade(avgCld);
                dado.setTemperaturaAmbiente(avgTmp);
                log.debug("Open-Meteo: atualizado dado existente | fazenda={} | data={}",
                        fazenda.getId(), day);
            } else if (existente.isPresent()) {

                log.debug("Open-Meteo: dia {} já possui dado da fonte '{}', pulando.",
                        day, existente.get().getFonte());
                return;
            } else {
                dado = DadoOrbital.builder()
                        .fazenda(fazenda)
                        .fonte(FONTE)
                        .dtColeta(day)
                        .irradianciaParTot(avgRad)
                        .nebulosidade(avgCld)
                        .temperaturaAmbiente(avgTmp)
                        .latitude(fazenda.getLatitude())
                        .longitude(fazenda.getLongitude())
                        .build();
            }

            dadoOrbitalRepository.save(dado);
            result.add(dado);
            log.debug("Open-Meteo salvo | fazenda={} | data={} | rad={} | cld={} | tmp={}",
                    fazenda.getId(), day, avgRad, avgCld, avgTmp);
        });

        return result;
    }


    private BigDecimal average(List<Double> values) {
        if (values == null || values.isEmpty()) return null;
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        return BigDecimal.valueOf(sum / values.size())
                .setScale(4, RoundingMode.HALF_UP);
    }
}