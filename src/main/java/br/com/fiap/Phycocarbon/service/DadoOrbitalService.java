package br.com.fiap.Phycocarbon.service;

import br.com.fiap.Phycocarbon.dto.NasaPowerDTOs;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
    private static final DateTimeFormatter NASA_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final double NASA_MISSING = -999.0;
    private static final int NASA_LAG_DAYS = 5;
    private static final int NASA_WINDOW_DAYS = 13;

    @Transactional
    public List<DadoOrbitalResponse> buscarEsalvarDadosNasa(Long fazendaId) {
        Fazenda fazenda = fazendaRepository.findById(fazendaId)
                .orElseThrow(() -> new EntityNotFoundException("Fazenda não encontrada: " + fazendaId));

        if (fazenda.getLatitude() == null || fazenda.getLongitude() == null) {
            throw new IllegalArgumentException("Fazenda não possui coordenadas geográficas cadastradas");
        }

        LocalDate fim    = LocalDate.now().minusDays(NASA_LAG_DAYS);
        LocalDate inicio = fim.minusDays(NASA_WINDOW_DAYS);

        String start = inicio.format(NASA_FMT);
        String end   = fim.format(NASA_FMT);

        String url = UriComponentsBuilder.fromUriString(NASA_POWER_URL)
                .queryParam("parameters", "ALLSKY_SFC_SW_DWN,T2M,CLOUD_AMT")
                .queryParam("community", "AG")
                .queryParam("longitude", fazenda.getLongitude().toPlainString())
                .queryParam("latitude",  fazenda.getLatitude().toPlainString())
                .queryParam("start", start)
                .queryParam("end",   end)
                .queryParam("format", "JSON")
                .toUriString();

        log.info("Chamando NASA POWER API | fazenda={} | período={} → {} | URL: {}",
                fazendaId, start, end, url);

        NasaPowerDTOs nasaResponse = restTemplate.getForObject(url, NasaPowerDTOs.class);

        if (nasaResponse == null || nasaResponse.properties() == null) {
            log.error("NASA POWER retornou resposta nula ou sem 'properties'. URL chamada: {}", url);
            throw new RuntimeException("NASA POWER API não retornou dados para as coordenadas informadas");
        }

        Map<String, Map<String, Double>> parametros = nasaResponse.properties().parameter();

        if (parametros == null || parametros.isEmpty()) {
            log.warn("NASA POWER retornou 'parameter' vazio para fazenda={} no período {}-{}.",
                    fazendaId, start, end);
            return dadoOrbitalRepository
                    .findByFazendaIdOrderByDtColetaDesc(fazendaId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        log.info("NASA POWER parâmetros recebidos: {}", parametros.keySet());

        Map<String, Double> par   = parametros.getOrDefault("ALLSKY_SFC_SW_DWN", Collections.emptyMap());
        Map<String, Double> temp  = parametros.getOrDefault("T2M",               Collections.emptyMap());
        Map<String, Double> cloud = parametros.getOrDefault("CLOUD_AMT",          Collections.emptyMap());

        if (par.isEmpty()) {
            log.warn("Parâmetro ALLSKY_SFC_SW_DWN ausente na resposta da NASA para fazenda={}", fazendaId);
        }

        par.forEach((dataStr, valorPar) -> {
            if (valorPar == null || Double.compare(valorPar, NASA_MISSING) == 0) {
                log.debug("Dado SW_DWN ausente (missing={}) para data={}, pulando.", NASA_MISSING, dataStr);
                return;
            }

            LocalDate data;
            try {
                data = LocalDate.parse(dataStr, NASA_FMT);
            } catch (Exception e) {
                log.warn("Data inválida recebida da NASA: '{}', pulando.", dataStr);
                return;
            }

            Optional<DadoOrbital> existente = dadoOrbitalRepository.findByFazendaIdAndDtColeta(fazendaId, data);
            if (existente.isPresent()) {
                log.debug("Dado para fazenda={} data={} já existe, ignorando duplicata.", fazendaId, data);
                return;
            }

            Double valorTemp  = temp.get(dataStr);
            Double valorCloud = cloud.get(dataStr);

            DadoOrbital dado = DadoOrbital.builder()
                    .fazenda(fazenda)
                    .fonte(FONTE)
                    .dtColeta(data)
                    .irradianciaParTot(BigDecimal.valueOf(valorPar))
                    .temperaturaAmbiente(valorTemp  != null && Double.compare(valorTemp,  NASA_MISSING) != 0
                            ? BigDecimal.valueOf(valorTemp)  : null)
                    .nebulosidade(valorCloud != null && Double.compare(valorCloud, NASA_MISSING) != 0
                            ? BigDecimal.valueOf(valorCloud) : null)
                    .latitude(fazenda.getLatitude())
                    .longitude(fazenda.getLongitude())
                    .build();

            dadoOrbitalRepository.save(dado);
            log.debug("Salvo dado orbital | fazenda={} | data={} | SW_DWN={} | T2M={} | CLOUD={}",
                    fazendaId, data, valorPar, valorTemp, valorCloud);
        });

        List<DadoOrbitalResponse> resultado = dadoOrbitalRepository
                .findByFazendaIdOrderByDtColetaDesc(fazendaId)
                .stream()
                .map(this::toResponse)
                .toList();

        log.info("Sincronização concluída | fazenda={} | {} registros no banco.", fazendaId, resultado.size());
        return resultado;
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public DadoOrbitalResponse buscarUltimo(Long fazendaId) {
        return dadoOrbitalRepository
                .findTopByFazendaIdOrderByDtColetaDesc(fazendaId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nenhum dado orbital encontrado para a fazenda: " + fazendaId));
    }

    public DadoOrbitalResponse toResponse(DadoOrbital d) {
        return new DadoOrbitalResponse(
                d.getId(),
                d.getFazenda().getId(),
                d.getFazenda().getNome(),
                d.getFonte(),
                d.getDtColeta(),
                d.getIrradianciaParTot(),
                d.getNebulosidade(),
                d.getTemperaturaAmbiente(),
                d.getLatitude(),
                d.getLongitude(),
                d.getDtRegistro()
        );
    }
}