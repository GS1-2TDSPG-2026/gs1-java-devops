package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.ResponseDTOs.DadoOrbitalResponse;
import br.com.fiap.aquaorbital.service.OpenMeteoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/dados-orbitais/open-meteo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Dados Orbitais — Open-Meteo",
        description = "Integração em tempo real com Open-Meteo Forecast API " +
                "(irradiância, nebulosidade, temperatura)"
)
public class OpenMeteoController {

    private final OpenMeteoService openMeteoService;


    @PostMapping("/fazenda/{fazendaId}/sincronizar")
    @Operation(
            summary = "Sincronizar previsão Open-Meteo para a fazenda",
            description = "Busca shortwave_radiation, cloud_cover, temperature_2m e " +
                    "relative_humidity_2m para os próximos 2 dias e salva em " +
                    "TB_DADO_ORBITAL com fonte=OPEN_METEO. " +
                    "Chamado a cada leitura dos sensores (tempo real)."
    )
    public ResponseEntity<List<DadoOrbitalResponse>> sincronizar(
            @PathVariable Long fazendaId) {
        return ResponseEntity.ok(
                openMeteoService.buscarESalvarDadosOpenMeteo(fazendaId));
    }
}