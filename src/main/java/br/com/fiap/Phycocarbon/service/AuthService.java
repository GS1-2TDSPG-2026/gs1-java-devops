package br.com.fiap.Phycocarbon.service;

import br.com.fiap.Phycocarbon.dto.AuthDTOs.LoginRequest;
import br.com.fiap.Phycocarbon.dto.AuthDTOs.RegisterRequest;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.TokenResponse;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.UsuarioResponse;
import br.com.fiap.Phycocarbon.entity.Perfil;
import br.com.fiap.Phycocarbon.entity.Usuario;
import br.com.fiap.Phycocarbon.repository.PerfilRepository;
import br.com.fiap.Phycocarbon.repository.UsuarioRepository;
import br.com.fiap.Phycocarbon.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        String token = jwtService.gerarToken(usuario);
        return new TokenResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().getNomePerfil()
        );
    }

    public UsuarioResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado: " + request.email());
        }

        Perfil perfil = perfilRepository.findByNomePerfil(request.nomePerfil())
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado: " + request.nomePerfil()));

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .telefone(request.telefone())
                .perfil(perfil)
                .build();

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getEmail(), u.getTelefone(),
                u.getPerfil().getNomePerfil(), u.getStatus(), u.getCriadoEm()
        );
    }
}
