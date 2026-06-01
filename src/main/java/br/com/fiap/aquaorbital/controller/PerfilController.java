package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.PerfilDTO;
import br.com.fiap.aquaorbital.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfis", description = "Gestão de perfis de acesso")
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    @Operation(summary = "Listar todos os perfis")
    public ResponseEntity<List<PerfilDTO>> listar() {
        return ResponseEntity.ok(perfilService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar perfil por ID")
    public ResponseEntity<PerfilDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo perfil")
    public ResponseEntity<PerfilDTO> criar(@Valid @RequestBody CriarPerfilRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perfilService.criar(request.nomePerfil(), request.descricao()));
    }

    public record CriarPerfilRequest(
            @NotBlank(message = "Nome do perfil é obrigatório") String nomePerfil,
            String descricao
    ) {}
}