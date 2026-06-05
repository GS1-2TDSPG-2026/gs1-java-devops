package br.com.fiap.Phycocarbon.service;

import br.com.fiap.Phycocarbon.dto.PerfilDTO;
import br.com.fiap.Phycocarbon.entity.Perfil;
import br.com.fiap.Phycocarbon.repository.PerfilRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;

    public List<PerfilDTO> listarTodos() {
        return perfilRepository.findAll()
                .stream()
                .map(p -> new PerfilDTO(p.getId(), p.getNomePerfil(), p.getDescricao()))
                .toList();
    }

    public PerfilDTO buscarPorId(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado: " + id));
        return new PerfilDTO(perfil.getId(), perfil.getNomePerfil(), perfil.getDescricao());
    }

    @Transactional
    public PerfilDTO criar(String nomePerfil, String descricao) {
        if (perfilRepository.existsByNomePerfil(nomePerfil)) {
            throw new IllegalArgumentException("Perfil já existe: " + nomePerfil);
        }
        Perfil perfil = Perfil.builder()
                .nomePerfil(nomePerfil)
                .descricao(descricao)
                .build();
        Perfil salvo = perfilRepository.save(perfil);
        return new PerfilDTO(salvo.getId(), salvo.getNomePerfil(), salvo.getDescricao());
    }
}