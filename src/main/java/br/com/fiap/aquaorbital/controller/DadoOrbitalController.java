package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.ResponseDTOs.DadoOrbitalResponse;
import br.com.fiap.aquaorbital.service.DadoOrbitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dados-orbitais")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dados Orbitais", description = "Integração com NASA POWER API — irradiância PAR, temperatura e nebulosidade")
public class DadoOrbitalController {

    private final DadoOrbitalService dadoOrbitalService;

    @PostMapping("/fazenda/{fazendaId}/sincronizar")
    @Operation(
            summary = "Sincronizar dados da NASA para a fazenda",
            description = "Chama a NASA POWER API com as coordenadas da fazenda e salva os dados do mês atual no Oracle"
    )
    public ResponseEntity<List<DadoOrbitalResponse>> sincronizar(@PathVariable Long fazendaId) {
        return ResponseEntity.ok(dadoOrbitalService.buscarEsalvarDadosNasa(fazendaId));
    }

    @GetMapping("/fazenda/{fazendaId}")
    @Operation(summary = "Listar todos os dados orbitais de uma fazenda")
    public ResponseEntity<List<DadoOrbitalResponse>> listarPorFazenda(@PathVariable Long fazendaId) {
        return ResponseEntity.ok(dadoOrbitalService.listarPorFazenda(fazendaId));
    }

    @GetMapping("/fazenda/{fazendaId}/ultimo")
    @Operation(summary = "Buscar o dado orbital mais recente da fazenda")
    public ResponseEntity<DadoOrbitalResponse> buscarUltimo(@PathVariable Long fazendaId) {
        return ResponseEntity.ok(dadoOrbitalService.buscarUltimo(fazendaId));
    }

    @GetMapping("/fazenda/{fazendaId}/periodo")
    @Operation(summary = "Listar dados orbitais por período")
    public ResponseEntity<List<DadoOrbitalResponse>> listarPorPeriodo(
            @PathVariable Long fazendaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(dadoOrbitalService.listarPorPeriodo(fazendaId, inicio, fim));
    }
}
