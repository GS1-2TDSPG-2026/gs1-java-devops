package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.repository.FazendaRepository;
import br.com.fiap.aquaorbital.service.Era5ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dados-orbitais/era5")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Dados Orbitais — ERA5 Archive",
        description = "Carga histórica ERA5 (2020–2024) via Open-Meteo Archive API. " +
                "Executar 1x para popular TB_DADO_ORBITAL para treino de modelos."
)
public class Era5ArchiveController {

    private final Era5ArchiveService era5ArchiveService;
    private final FazendaRepository  fazendaRepository;

    @PostMapping("/fazenda/{fazendaId}/carregar")
    @Operation(
            summary = "Carga histórica ERA5 — período padrão (2020–2024)",
            description = "Busca ~5 anos de dados horários e salva as médias diárias em " +
                    "TB_DADO_ORBITAL (fonte=ERA5_ARCHIVE). Idempotente: dias já " +
                    "existentes são ignorados."
    )
    public ResponseEntity<Map<String, Object>> carregarPeriodoPadrao(
            @PathVariable Long fazendaId) {

        int inseridos = era5ArchiveService.carregarHistorico(fazendaId);
        return ResponseEntity.ok(resposta(fazendaId, inseridos));
    }

    @PostMapping("/fazenda/{fazendaId}/carregar/periodo")
    @Operation(
            summary = "Carga histórica ERA5 — período customizado",
            description = "Permite informar start_date e end_date para controle granular " +
                    "da carga. Útil para carregar um ano por vez em bases grandes."
    )
    public ResponseEntity<Map<String, Object>> carregarPeriodoCustom(
            @PathVariable Long fazendaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        int inseridos = era5ArchiveService.carregarHistorico(fazendaId, startDate, endDate);
        return ResponseEntity.ok(resposta(fazendaId, inseridos));
    }

    @PostMapping("/carregar-todas")
    @Operation(
            summary = "Carga histórica ERA5 — todas as fazendas",
            description = "Itera sobre todas as fazendas e executa a carga do período " +
                    "padrão para cada uma. Operação longa — considere executar " +
                    "fora do horário de pico."
    )
    public ResponseEntity<Map<String, Object>> carregarTodasFazendas() {

        var fazendas       = fazendaRepository.findAll();
        int totalInseridos = 0;
        int erros          = 0;

        for (var fazenda : fazendas) {
            try {
                totalInseridos += era5ArchiveService.carregarHistorico(fazenda.getId());
            } catch (Exception e) {
                erros++;
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fazendasProcessadas", fazendas.size());
        body.put("diasInseridos", totalInseridos);
        body.put("erros", erros);
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> resposta(Long fazendaId, int inseridos) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fazendaId", fazendaId);
        body.put("diasInseridos", inseridos);
        body.put("fonte", "ERA5_ARCHIVE");
        return body;
    }
}