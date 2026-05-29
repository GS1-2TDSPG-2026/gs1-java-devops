package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.TanqueDTOs.AtualizarTanqueRequest;
import br.com.fiap.aquaorbital.dto.TanqueDTOs.CriarTanqueRequest;
import br.com.fiap.aquaorbital.dto.ResponseDTOs.TanqueResponse;
import br.com.fiap.aquaorbital.service.TanqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/tanques")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tanques", description = "Gestão de tanques e biofotorreatores")
public class TanqueController {

    private final TanqueService tanqueService;

    @PostMapping
    @Operation(summary = "Criar novo tanque")
    public ResponseEntity<EntityModel<TanqueResponse>> criar(@Valid @RequestBody CriarTanqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(tanqueService.criar(request)));
    }

    @GetMapping
    @Operation(summary = "Listar todos os tanques (paginado)")
    public ResponseEntity<Page<TanqueResponse>> listar(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(tanqueService.listarTodos(pageable));
    }

    @GetMapping("/fazenda/{fazendaId}")
    @Operation(summary = "Listar tanques por fazenda")
    public ResponseEntity<List<TanqueResponse>> listarPorFazenda(@PathVariable Long fazendaId) {
        return ResponseEntity.ok(tanqueService.listarPorFazenda(fazendaId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tanque por ID")
    public ResponseEntity<EntityModel<TanqueResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(tanqueService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tanque")
    public ResponseEntity<EntityModel<TanqueResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarTanqueRequest request) {
        return ResponseEntity.ok(toModel(tanqueService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover tanque")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tanqueService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<TanqueResponse> toModel(TanqueResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(TanqueController.class).buscar(response.id())).withSelfRel(),
                linkTo(methodOn(TanqueController.class).listarPorFazenda(response.idFazenda())).withRel("tanques-fazenda"),
                linkTo(methodOn(FazendaController.class).buscar(response.idFazenda())).withRel("fazenda")
        );
    }
}