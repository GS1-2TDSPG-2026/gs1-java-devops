package br.com.fiap.Phycocarbon.controller;

import br.com.fiap.Phycocarbon.dto.FazendaDTOs.AtualizarFazendaRequest;
import br.com.fiap.Phycocarbon.dto.FazendaDTOs.CriarFazendaRequest;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.DashboardFazendaResponse;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.FazendaResponse;
import br.com.fiap.Phycocarbon.service.FazendaService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/fazendas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fazendas", description = "Gestão de fazendas biológicas")
public class FazendaController {

    private final FazendaService fazendaService;

    @PostMapping
    @Operation(summary = "Criar nova fazenda")
    public ResponseEntity<EntityModel<FazendaResponse>> criar(
            @Valid @RequestBody CriarFazendaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {


        Long usuarioId = ((br.com.fiap.Phycocarbon.entity.Usuario) userDetails).getId();
        FazendaResponse response = fazendaService.criar(request, usuarioId);
        EntityModel<FazendaResponse> model = toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping
    @Operation(summary = "Listar todas as fazendas (paginado)")
    public ResponseEntity<Page<FazendaResponse>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(fazendaService.listarTodas(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fazenda por ID")
    public ResponseEntity<EntityModel<FazendaResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(fazendaService.buscarPorId(id)));
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Dashboard da fazenda — status, tanques, previsões e créditos")
    public ResponseEntity<DashboardFazendaResponse> dashboard(@PathVariable Long id) {
        return ResponseEntity.ok(fazendaService.getDashboard(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fazenda")
    public ResponseEntity<EntityModel<FazendaResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarFazendaRequest request) {
        return ResponseEntity.ok(toModel(fazendaService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover fazenda")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fazendaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<FazendaResponse> toModel(FazendaResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(FazendaController.class).buscar(response.id())).withSelfRel(),
                linkTo(methodOn(FazendaController.class).dashboard(response.id())).withRel("dashboard"),
                linkTo(methodOn(TanqueController.class).listarPorFazenda(response.id())).withRel("tanques"),
                linkTo(methodOn(FazendaController.class).listar(Pageable.unpaged())).withRel("fazendas")
        );
    }
}
