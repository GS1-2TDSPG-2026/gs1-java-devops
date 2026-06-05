package br.com.fiap.Phycocarbon.service;

import br.com.fiap.Phycocarbon.dto.UsuarioDTO;
import br.com.fiap.Phycocarbon.entity.Usuario;
import br.com.fiap.Phycocarbon.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public Page<UsuarioDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::toDTO);
    }

    public UsuarioDTO buscarPorId(Long id) {
        return toDTO(usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id)));
    }

    public Page<UsuarioDTO> buscarPorPerfil(Long idPerfil, Pageable pageable) {
        return usuarioRepository.findByPerfilId(idPerfil, pageable).map(this::toDTO);
    }

    @Transactional
    public UsuarioDTO atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        if (request.nome() != null && !request.nome().isBlank()) usuario.setNome(request.nome());
        if (request.email() != null && !request.email().isBlank()) usuario.setEmail(request.email());
        if (request.telefone() != null && !request.telefone().isBlank()) usuario.setTelefone(request.telefone());
        if (request.status() != null && !request.status().isBlank()) usuario.setStatus(request.status());

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioDTO toDTO(Usuario u) {
        return new UsuarioDTO(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getTelefone(),
                u.getStatus(),
                u.getDtCriacao(),
                u.getPerfil().getId(),
                u.getPerfil().getNomePerfil(),
                u.getPerfil().getDescricao()
        );
    }

    public record AtualizarUsuarioRequest(String nome, String email, String telefone, String status) {}
}