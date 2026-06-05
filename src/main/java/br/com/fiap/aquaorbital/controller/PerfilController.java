package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.PerfilDTO;
import br.com.fiap.aquaorbital.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfis", description = "Gestão de perfis de acesso")
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    @Operation(summary = "Listar todos os perfis")
    public ResponseEntity<CollectionModel<EntityModel<PerfilDTO>>> listar() {
        List<EntityModel<PerfilDTO>> perfis = perfilService.listarTodos().stream()
                .map(this::toModel)
                .toList();

        CollectionModel<EntityModel<PerfilDTO>> collection = CollectionModel.of(perfis,
                linkTo(methodOn(PerfilController.class).listar()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar perfil por ID")
    public ResponseEntity<EntityModel<PerfilDTO>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(perfilService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Criar novo perfil")
    public ResponseEntity<EntityModel<PerfilDTO>> criar(@Valid @RequestBody CriarPerfilRequest request) {
        PerfilDTO criado = perfilService.criar(request.nomePerfil(), request.descricao());
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(criado));
    }

    // ---

    private EntityModel<PerfilDTO> toModel(PerfilDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(PerfilController.class).buscar(dto.id())).withSelfRel(),
                linkTo(methodOn(PerfilController.class).listar()).withRel("perfis"),
                linkTo(methodOn(UsuarioController.class).buscarPorPerfil(dto.id(), null)).withRel("usuarios"));
    }

    public record CriarPerfilRequest(
            @NotBlank(message = "Nome do perfil é obrigatório") String nomePerfil,
            String descricao
    ) {}
}