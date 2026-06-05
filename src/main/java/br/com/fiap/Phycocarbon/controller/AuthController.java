package br.com.fiap.Phycocarbon.controller;

import br.com.fiap.Phycocarbon.dto.AuthDTOs.LoginRequest;
import br.com.fiap.Phycocarbon.dto.AuthDTOs.RegisterRequest;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.TokenResponse;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.UsuarioResponse;
import br.com.fiap.Phycocarbon.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e cadastro de usuários")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Retorna token JWT para uso nas demais rotas")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}
