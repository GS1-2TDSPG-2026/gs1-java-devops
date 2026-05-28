package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.request.AuthDTOs.LoginRequest;
import br.com.fiap.aquaorbital.dto.request.AuthDTOs.RegisterRequest;
import br.com.fiap.aquaorbital.dto.response.ResponseDTOs.TokenResponse;
import br.com.fiap.aquaorbital.dto.response.ResponseDTOs.UsuarioResponse;
import br.com.fiap.aquaorbital.service.AuthService;
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
    @Operation(summary = "Realizar login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}