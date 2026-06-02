package br.com.fiap.aquaorbital.service;

import br.com.fiap.aquaorbital.dto.OpenMeteoDTOs;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Era5ArchiveService {

    private final DadoOrbitalRepository dadoOrbitalRepository;
    private final FazendaRepository     fazendaRepository;
    private final RestTemplate          restTemplate;
    private final DadoOrbitalService    dadoOrbitalService;

    private static final String ARCHIVE_URL = "https://archive-api.open-meteo.com/v1/archive";
    private static final String FONTE       = "ERA5_ARCHIVE";
    private static final String TIMEZONE    = "America/Sao_Paulo";

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Transactional
    public int carregarHistorico(Long fazendaId, LocalDate startDate, LocalDate endDate) {

        Fazenda fazenda = fazendaRepository.findById(fazendaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fazenda não encontrada: " + fazendaId));

        if (fazenda.getLatitude() == null || fazenda.getLongitude() == null) {
            throw new IllegalArgumentException(
                    "Fazenda não possui coordenadas geográficas cadastradas");
        }

        String url = buildUrl(fazenda, startDate, endDate);
        log.info("[ ERA5 ] Iniciando carga histórica | fazenda={} | {} → {} | URL: {}",
                fazendaId, startDate, endDate, url);

        OpenMeteoDTOs response = restTemplate.getForObject(url, OpenMeteoDTOs.class);

        if (response == null || response.hourly() == null) {
            log.error("[ ERA5 ] Resposta nula da Archive API. URL: {}", url);
            throw new RuntimeException(
                    "Open-Meteo Archive API não retornou dados para as coordenadas informadas");
        }

        int inseridos = processarHourly(fazenda, response.hourly());
        log.info("[ ERA5 ] Carga concluída | fazenda={} | {} dias inseridos.", fazendaId, inseridos);
        return inseridos;
    }

    @Transactional
    public int carregarHistorico(Long fazendaId) {
        return carregarHistorico(
                fazendaId,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    private String buildUrl(Fazenda fazenda, LocalDate start, LocalDate end) {
        return UriComponentsBuilder.fromUriString(ARCHIVE_URL)
                .queryParam("latitude",   fazenda.getLatitude().toPlainString())
                .queryParam("longitude",  fazenda.getLongitude().toPlainString())
                .queryParam("start_date", start.toString())
                .queryParam("end_date",   end.toString())
                .queryParam("hourly",
                        "shortwave_radiation,cloud_cover,temperature_2m,relative_humidity_2m")
                .queryParam("timezone", TIMEZONE)
                .toUriString();
    }

    private int processarHourly(Fazenda fazenda, OpenMeteoDTOs.HourlyData hourly) {

        List<String> times      = hourly.time();
        List<Double> radiations = hourly.shortwaveRadiation();
        List<Double> clouds     = hourly.cloudCover();
        List<Double> temps      = hourly.temperature2m();

        if (times == null || times.isEmpty()) {
            log.warn("[ ERA5 ] Lista 'time' vazia para fazenda={}", fazenda.getId());
            return 0;
        }

        record Acc(List<Double> rad, List<Double> cld, List<Double> tmp) {}
        java.util.Map<LocalDate, Acc> byDay = new java.util.LinkedHashMap<>();

        for (int i = 0; i < times.size(); i++) {
            LocalDateTime ldt;
            try {
                ldt = LocalDateTime.parse(times.get(i), DT_FMT);
            } catch (Exception e) {
                log.warn("[ ERA5 ] Timestamp inválido '{}', ignorando.", times.get(i));
                continue;
            }
            LocalDate day = ldt.toLocalDate();
            byDay.computeIfAbsent(day, d ->
                    new Acc(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

            Acc acc = byDay.get(day);
            if (radiations != null && i < radiations.size() && radiations.get(i) != null)
                acc.rad().add(radiations.get(i));
            if (clouds != null && i < clouds.size() && clouds.get(i) != null)
                acc.cld().add(clouds.get(i));
            if (temps != null && i < temps.size() && temps.get(i) != null)
                acc.tmp().add(temps.get(i));
        }

        int inseridos = 0;

        for (var entry : byDay.entrySet()) {
            LocalDate day = entry.getKey();
            Acc       acc = entry.getValue();

            if (dadoOrbitalRepository
                    .findByFazendaIdAndDtColeta(fazenda.getId(), day)
                    .isPresent()) {
                log.debug("[ ERA5 ] Dia {} já existe para fazenda={}, ignorando.",
                        day, fazenda.getId());
                continue;
            }

            DadoOrbital dado = DadoOrbital.builder()
                    .fazenda(fazenda)
                    .fonte(FONTE)
                    .dtColeta(day)
                    .irradianciaParTot(average(acc.rad()))
                    .nebulosidade(average(acc.cld()))
                    .temperaturaAmbiente(average(acc.tmp()))
                    .latitude(fazenda.getLatitude())
                    .longitude(fazenda.getLongitude())
                    .build();

            dadoOrbitalRepository.save(dado);
            inseridos++;

            if (inseridos % 365 == 0) {
                log.info("[ ERA5 ] fazenda={} — {} dias inseridos até agora...",
                        fazenda.getId(), inseridos);
            }
        }

        return inseridos;
    }

    private BigDecimal average(List<Double> values) {
        if (values == null || values.isEmpty()) return null;
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        return BigDecimal.valueOf(sum / values.size())
                .setScale(4, RoundingMode.HALF_UP);
    }
}