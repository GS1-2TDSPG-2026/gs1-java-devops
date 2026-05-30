package br.com.fiap.aquaorbital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTOs {

    public record LoginRequest(
            @NotBlank(message = "Email é obrigatório")
            @Email(message = "Email inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            String senha
    ) {}

    public record RegisterRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
            String nome,

            @NotBlank(message = "Email é obrigatório")
            @Email(message = "Email inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
            String senha,

            String telefone,

            @NotBlank(message = "Perfil é obrigatório")
            String nomePerfil
    ) {}
}
