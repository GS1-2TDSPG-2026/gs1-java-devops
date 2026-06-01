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

    // NASA POWER tem delay de ~7 dias — busca o mês anterior completo
    // para garantir dados reais disponíveis sem valores -999
    private static final int DIAS_DELAY_NASA = 7;
    private static final int JANELA_DIAS = 30;

    @Transactional
    public List<DadoOrbitalResponse> buscarEsalvarDadosNasa(Long fazendaId) {
        Fazenda fazenda = fazendaRepository.findById(fazendaId)
                .orElseThrow(() -> new EntityNotFoundException("Fazenda não encontrada: " + fazendaId));

        if (fazenda.getLatitude() == null || fazenda.getLongitude() == null) {
            throw new IllegalArgumentException(
                    "Fazenda sem coordenadas geográficas. Atualize latitude e longitude antes de sincronizar.");
        }

        // Ajusta o período para o delay da NASA:
        // fim  = hoje - 7 dias (último dia com dado garantido)
        // início = fim - 30 dias (janela de 30 dias de dados reais)
        LocalDate fim = LocalDate.now().minusDays(DIAS_DELAY_NASA);
        LocalDate inicio = fim.minusDays(JANELA_DIAS - 1);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        String start = inicio.format(fmt);
        String end   = fim.format(fmt);

        String url = UriComponentsBuilder.fromUriString(NASA_POWER_URL)
                .queryParam("parameters", "ALLSKY_SFC_PAR_TOT,T2M,CLOUD_AMT")
                .queryParam("community", "AG")
                .queryParam("longitude", fazenda.getLongitude().toPlainString())
                .queryParam("latitude",  fazenda.getLatitude().toPlainString())
                .queryParam("start", start)
                .queryParam("end",   end)
                .queryParam("format", "JSON")
                .toUriString();

        log.info("Chamando NASA POWER | fazenda={} | período={} → {} | url={}",
                fazendaId, start, end, url);

        NasaPowerDTOs nasaResponse = restTemplate.getForObject(url, NasaPowerDTOs.class);

        if (nasaResponse == null || nasaResponse.properties() == null) {
            throw new RuntimeException(
                    "NASA POWER API não retornou dados. Verifique as coordenadas da fazenda.");
        }

        Map<String, Map<String, Double>> parametros = nasaResponse.properties().parameter();

        if (parametros == null || !parametros.containsKey("ALLSKY_SFC_PAR_TOT")) {
            throw new RuntimeException(
                    "Resposta da NASA POWER não contém o parâmetro ALLSKY_SFC_PAR_TOT esperado.");
        }

        Map<String, Double> par   = parametros.get("ALLSKY_SFC_PAR_TOT");
        Map<String, Double> temp  = parametros.get("T2M");
        Map<String, Double> cloud = parametros.get("CLOUD_AMT");

        int[] contadores = {0, 0}; // [salvos, pulados]

        par.forEach((dataStr, valorPar) -> {
            // Ignora chave "ANN" (média anual) e outras não-datas que a NASA inclui
            if (dataStr.length() != 8) return;

            LocalDate data;
            try {
                data = LocalDate.parse(dataStr, fmt);
            } catch (Exception e) {
                log.warn("Data inválida retornada pela NASA: {}", dataStr);
                return;
            }

            // Pula dados inválidos (-999 = sem cobertura de satélite)
            if (valorPar == null || valorPar <= -998.0) {
                contadores[1]++;
                return;
            }

            // Não duplica registros já existentes
            Optional<DadoOrbital> existente =
                    dadoOrbitalRepository.findByFazendaIdAndDtColeta(fazendaId, data);
            if (existente.isPresent()) {
                contadores[1]++;
                return;
            }

            Double valorTemp  = (temp  != null) ? temp.get(dataStr)  : null;
            Double valorCloud = (cloud != null) ? cloud.get(dataStr) : null;

            DadoOrbital dado = DadoOrbital.builder()
                    .fazenda(fazenda)
                    .fonte(FONTE)
                    .dtColeta(data)
                    .irradianciaParTot(BigDecimal.valueOf(valorPar))
                    .temperaturaAmbiente(
                            (valorTemp != null && valorTemp > -998.0)
                                    ? BigDecimal.valueOf(valorTemp) : null)
                    .nebulosidade(
                            (valorCloud != null && valorCloud > -998.0)
                                    ? BigDecimal.valueOf(valorCloud) : null)
                    .latitude(fazenda.getLatitude())
                    .longitude(fazenda.getLongitude())
                    .build();

            dadoOrbitalRepository.save(dado);
            contadores[0]++;
        });

        log.info("NASA POWER sincronizado | fazenda={} | salvos={} | pulados/duplicados={}",
                fazendaId, contadores[0], contadores[1]);

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