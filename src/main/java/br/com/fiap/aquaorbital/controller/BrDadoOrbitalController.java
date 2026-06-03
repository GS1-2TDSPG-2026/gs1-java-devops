package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.BrDadoOrbitalDTOs;
import br.com.fiap.aquaorbital.service.BrDadoOrbitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dados-orbitais/inmet")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dados Orbitais BR", description = "Dados das estações meteorológicas INMET — sincronizados via InMetSyncScheduler")
public class BrDadoOrbitalController {

    private final BrDadoOrbitalService brDadoOrbitalService;


    @GetMapping
    @Operation(summary = "Listar todos os registros INMET salvos no banco")
    public ResponseEntity<List<BrDadoOrbitalDTOs>> listarTodos() {
        return ResponseEntity.ok(brDadoOrbitalService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar registro INMET por ID")
    public ResponseEntity<BrDadoOrbitalDTOs> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(brDadoOrbitalService.buscarPorId(id));
    }



    @GetMapping("/estacao/{codEstacao}")
    @Operation(summary = "Listar todos os registros de uma estação INMET")
    public ResponseEntity<List<BrDadoOrbitalDTOs>> listarPorEstacao(
            @PathVariable String codEstacao) {
        return ResponseEntity.ok(brDadoOrbitalService.listarPorEstacao(codEstacao));
    }

    @GetMapping("/estacao/{codEstacao}/ultimo")
    @Operation(summary = "Buscar o registro mais recente de uma estação INMET")
    public ResponseEntity<BrDadoOrbitalDTOs> buscarUltimo(
            @PathVariable String codEstacao) {
        return ResponseEntity.ok(brDadoOrbitalService.buscarUltimo(codEstacao));
    }

    @GetMapping("/estacao/{codEstacao}/periodo")
    @Operation(summary = "Listar registros de uma estação por período (yyyy-MM-dd)")
    public ResponseEntity<List<BrDadoOrbitalDTOs>> listarPorPeriodo(
            @PathVariable String codEstacao,
            @RequestParam String inicio,
            @RequestParam String fim) {
        return ResponseEntity.ok(brDadoOrbitalService.listarPorPeriodo(codEstacao, inicio, fim));
    }



    @PostMapping("/sincronizar")
    @Operation(
            summary     = "Sincronizar dados INMET manualmente",
            description = "Chama a API do INMET e salva registros novos — equivale a disparar o InMetSyncScheduler sem esperar o cron das 01:00."
    )
    public ResponseEntity<List<BrDadoOrbitalDTOs>> sincronizar(
            @RequestParam String codEstacao,
            @RequestParam(required = false) String data) {
        return ResponseEntity.ok(brDadoOrbitalService.sincronizar(codEstacao, data));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar um registro INMET por ID")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        brDadoOrbitalService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}