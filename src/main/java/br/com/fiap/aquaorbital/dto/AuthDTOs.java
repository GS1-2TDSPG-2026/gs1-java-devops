package br.com.fiap.aquaorbital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTOs {

    public record LoginRequest(
            @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
            @NotBlank(message = "Senha é obrigatória") String senha
    ) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 100) String nome,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String senha,
            String telefone,
            @NotBlank String nomePerfil
    ) {}
}