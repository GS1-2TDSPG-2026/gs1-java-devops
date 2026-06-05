package br.com.fiap.Phycocarbon.dto;

import java.time.LocalDateTime;

public record UsuarioDTO(
        Long idUsuario,
        String nome,
        String email,
        String telefone,
        String status,
        LocalDateTime dtCriacao,
        Long idPerfil,
        String nomePerfil,
        String descricao
) {}