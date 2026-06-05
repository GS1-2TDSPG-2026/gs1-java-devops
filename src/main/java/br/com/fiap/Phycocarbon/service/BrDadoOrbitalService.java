package br.com.fiap.Phycocarbon.service;

import br.com.fiap.Phycocarbon.dto.BrDadoOrbitalDTOs;
import br.com.fiap.Phycocarbon.entity.BrDadoOrbital;
import br.com.fiap.Phycocarbon.repository.BrDadoOrbitalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrDadoOrbitalService {

    private final BrDadoOrbitalRepository repository;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "https://apitempo.inmet.gov.br/ESTACAO";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Transactional(readOnly = true)
    public List<BrDadoOrbitalDTOs> listarTodos() {
        return repository.findAll()
                .stream().map(BrDadoOrbitalDTOs::from).toList();
    }

    @Transactional(readOnly = true)
    public BrDadoOrbitalDTOs buscarPorId(Long id) {
        return repository.findById(id)
                .map(BrDadoOrbitalDTOs::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Registro INMET não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<BrDadoOrbitalDTOs> listarPorEstacao(String codEstacao) {
        return repository
                .findByCodEstacaoOrderByDataMedicaoDescHoraMedicaoDesc(codEstacao)
                .stream().map(BrDadoOrbitalDTOs::from).toList();
    }

    @Transactional(readOnly = true)
    public BrDadoOrbitalDTOs buscarUltimo(String codEstacao) {
        return repository
                .findFirstByCodEstacaoOrderByDataMedicaoDescHoraMedicaoDesc(codEstacao)
                .map(BrDadoOrbitalDTOs::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nenhum dado para estação: " + codEstacao));
    }

    @Transactional(readOnly = true)
    public List<BrDadoOrbitalDTOs> listarPorPeriodo(
            String codEstacao, String inicio, String fim) {
        return repository
                .findByCodEstacaoAndDataMedicaoBetweenOrderByDataMedicaoDescHoraMedicaoDesc(
                        codEstacao, inicio, fim)
                .stream().map(BrDadoOrbitalDTOs::from).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarEstacoesDistintas() {
        return repository.findEstacoesDistintas();
    }


    @Transactional
    public List<BrDadoOrbitalDTOs> sincronizar(String codEstacao, String data) {
        String targetDate = (data != null && !data.isBlank())
                ? data
                : LocalDate.now().minusDays(1).format(FMT);

        log.info("[INMET] Sincronização manual | estação={} | data={}", codEstacao, targetDate);

        try {
            String url = String.format("%s/%s/%s/%s",
                    BASE_URL, targetDate, targetDate, codEstacao);

            log.debug("[INMET] GET {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "INMET retornou HTTP " + response.statusCode() + " → " + response.body());
            }

            List<BrDadoOrbital> novos = parseEpersistir(response.body(), codEstacao);
            log.info("[INMET] {} registros novos persistidos | estação={}", novos.size(), codEstacao);
            return novos.stream().map(BrDadoOrbitalDTOs::from).toList();

        } catch (Exception e) {
            log.error("[INMET] Falha na sincronização manual | estação={}: {}", codEstacao, e.getMessage(), e);
            throw new RuntimeException("Erro ao sincronizar dados INMET: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException("Registro INMET não encontrado: " + id);
        repository.deleteById(id);
    }



    private List<BrDadoOrbital> parseEpersistir(String json, String codEstacao) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        List<BrDadoOrbital> salvos = new ArrayList<>();

        if (!root.isArray()) {
            log.warn("[INMET] Resposta não é array para estação={}", codEstacao);
            return salvos;
        }

        for (JsonNode node : root) {
            String dtMed = texto(node, "DT_MEDICAO");
            String hrMed = texto(node, "HR_MEDICAO");


            if (repository.existsByCodEstacaoAndDataMedicaoAndHoraMedicao(
                    codEstacao, dtMed, hrMed)) {
                continue;
            }

            BrDadoOrbital dado = BrDadoOrbital.builder()
                    .codEstacao(texto(node, "CD_ESTACAO"))
                    .nomeEstacao(texto(node, "DC_NOME"))
                    .dataMedicao(dtMed)
                    .horaMedicao(hrMed)
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

            salvos.add(repository.save(dado));
        }
        return salvos;
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