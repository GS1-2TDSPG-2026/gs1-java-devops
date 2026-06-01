package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.UsuarioDTO;
import br.com.fiap.aquaorbital.service.UsuarioService;
import br.com.fiap.aquaorbital.service.UsuarioService.AtualizarUsuarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuários", description = "Gestão de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<Page<UsuarioDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping("/perfil/{idPerfil}")
    @Operation(summary = "Listar usuários por perfil")
    public ResponseEntity<Page<UsuarioDTO>> buscarPorPerfil(@PathVariable Long idPerfil, Pageable pageable) {
        return ResponseEntity.ok(usuarioService.buscarPorPerfil(idPerfil, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id,
                                                @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}