package br.com.fiap.Phycocarbon.controller;

import br.com.fiap.Phycocarbon.dto.UsuarioDTO;
import br.com.fiap.Phycocarbon.service.UsuarioService;
import br.com.fiap.Phycocarbon.service.UsuarioService.AtualizarUsuarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuários", description = "Gestão de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PagedResourcesAssembler<UsuarioDTO> pagedAssembler;

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<PagedModel<EntityModel<UsuarioDTO>>> listar(Pageable pageable) {
        Page<UsuarioDTO> page = usuarioService.listarTodos(pageable);
        PagedModel<EntityModel<UsuarioDTO>> model = pagedAssembler.toModel(page, this::toModel);
        return ResponseEntity.ok(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<EntityModel<UsuarioDTO>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(usuarioService.buscarPorId(id)));
    }

    @GetMapping("/perfil/{idPerfil}")
    @Operation(summary = "Listar usuários por perfil")
    public ResponseEntity<PagedModel<EntityModel<UsuarioDTO>>> buscarPorPerfil(
            @PathVariable Long idPerfil, Pageable pageable) {
        Page<UsuarioDTO> page = usuarioService.buscarPorPerfil(idPerfil, pageable);
        PagedModel<EntityModel<UsuarioDTO>> model = pagedAssembler.toModel(page, this::toModel);
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<EntityModel<UsuarioDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(toModel(usuarioService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ---

    private EntityModel<UsuarioDTO> toModel(UsuarioDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(UsuarioController.class).buscar(dto.idUsuario())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listar(null)).withRel("usuarios"),
                linkTo(methodOn(PerfilController.class).buscar(dto.idPerfil())).withRel("perfil"));
    }
}