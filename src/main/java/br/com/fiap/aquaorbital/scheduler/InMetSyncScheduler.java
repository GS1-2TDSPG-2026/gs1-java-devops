package br.com.fiap.aquaorbital.scheduler;

import br.com.fiap.aquaorbital.entity.BrDadoOrbital;
import br.com.fiap.aquaorbital.repository.BrDadoOrbitalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Component
public class InMetSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(InMetSyncScheduler.class);

    private static final String BASE_URL = "https://apitempo.inmet.gov.br/ESTACAO";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Value("${inmet.estacao.codigo:A701}")
    private String codEstacao;


    @Value("${inmet.estacao.janela-dias:1}")
    private int janelaDias;

    private final BrDadoOrbitalRepository repository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public InMetSyncScheduler(BrDadoOrbitalRepository repository, ObjectMapper objectMapper) {
        this.repository   = repository;
        this.objectMapper = objectMapper;
        this.httpClient   = HttpClient.newHttpClient();
    }


    @Scheduled(cron = "${inmet.estacao.cron:0 0 1 * * *}", zone = "America/Sao_Paulo")
    public void executar() {
        LocalDate fim    = LocalDate.now().minusDays(1);          // ontem (dado já fechado)
        LocalDate inicio = fim.minusDays(janelaDias - 1);

        log.info("[INMET] Iniciando coleta | estação={} | período={} a {}", codEstacao, inicio, fim);

        try {
            List<BrDadoOrbital> registros = buscarDados(inicio, fim);
            if (registros.isEmpty()) {
                log.warn("[INMET] Nenhum registro retornado pela API para o período.");
                return;
            }
            repository.saveAll(registros);
            log.info("[INMET] {} registro(s) persistido(s) em TB_DADO_ORBITAL.", registros.size());
        } catch (Exception e) {
            log.error("[INMET] Falha na coleta dos dados meteorológicos.", e);
        }
    }


    private List<BrDadoOrbital> buscarDados(LocalDate inicio, LocalDate fim) throws Exception {
        String url = String.format("%s/%s/%s/%s",
                BASE_URL, inicio.format(FMT), fim.format(FMT), codEstacao);

        log.debug("[INMET] GET {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "INMET retornou HTTP " + response.statusCode() + " → " + response.body());
        }

        return parseResposta(response.body());
    }

    private List<BrDadoOrbital> parseResposta(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        List<BrDadoOrbital> lista = new ArrayList<>();

        if (!root.isArray()) {
            log.warn("[INMET] Resposta inesperada (não é array): {}", json);
            return lista;
        }

        for (JsonNode node : root) {
            BrDadoOrbital dado = BrDadoOrbital.builder()
                    .codEstacao(texto(node, "CD_ESTACAO"))
                    .nomeEstacao(texto(node, "DC_NOME"))
                    .dataMedicao(texto(node, "DT_MEDICAO"))
                    .horaMedicao(texto(node, "HR_MEDICAO"))
                    .tempMaxima(decimal(node, "TEM_MAX"))
                    .tempMinima(decimal(node, "TEM_MIN"))
                    .tempMedia(decimal(node, "TEM_MED"))
                    .umidadeRelativa(decimal(node, "UMD_MED"))
                    .precipitacao(decimal(node, "CHUVA"))
                    .velocidadeVento(decimal(node, "VEN_VEL"))
                    .direcaoVento(decimal(node, "VEN_DIR"))
                    .pressaoAtm(decimal(node, "PRE_INS"))
                    .radiacaoGlobal(decimal(node, "RAD_GLO"))
                    .jsonOriginal(node.toString())
                    .build();

            lista.add(dado);
        }
        return lista;
    }

    private String texto(JsonNode node, String campo) {
        JsonNode n = node.get(campo);
        return (n != null && !n.isNull()) ? n.asText() : null;
    }

    private BigDecimal decimal(JsonNode node, String campo) {
        JsonNode n = node.get(campo);
        if (n == null || n.isNull() || n.asText().isBlank()) return null;
        try {
            return new BigDecimal(n.asText().replace(",", "."));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}