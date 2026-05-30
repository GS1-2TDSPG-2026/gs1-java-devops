package br.com.fiap.aquaorbital.service;

import br.com.fiap.aquaorbital.dto.TanqueDTOs.AtualizarTanqueRequest;
import br.com.fiap.aquaorbital.dto.TanqueDTOs.CriarTanqueRequest;
import br.com.fiap.aquaorbital.dto.ResponseDTOs.TanqueResponse;
import br.com.fiap.aquaorbital.entity.Fazenda;
import br.com.fiap.aquaorbital.entity.Tanque;
import br.com.fiap.aquaorbital.repository.FazendaRepository;
import br.com.fiap.aquaorbital.repository.TanqueRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TanqueService {

    private final TanqueRepository tanqueRepository;
    private final FazendaRepository fazendaRepository;

    @Transactional
    public TanqueResponse criar(CriarTanqueRequest request) {
        if (tanqueRepository.existsByCodigoTanque(request.codigoTanque())) {
            throw new IllegalArgumentException("Código de tanque já existe: " + request.codigoTanque());
        }

        Fazenda fazenda = fazendaRepository.findById(request.idFazenda())
                .orElseThrow(() -> new EntityNotFoundException("Fazenda não encontrada: " + request.idFazenda()));

        Tanque tanque = Tanque.builder()
                .fazenda(fazenda)
                .codigoTanque(request.codigoTanque())
                .tipoAlga(request.tipoAlga())
                .capacidadeLitros(request.capacidadeLitros())
                .phMin(request.phMin())
                .phMax(request.phMax())
                .temperaturaMin(request.temperaturaMin())
                .temperaturaMax(request.temperaturaMax())
                .dtInstalacao(request.dtInstalacao())
                .build();

        return toResponse(tanqueRepository.save(tanque));
    }

    public Page<TanqueResponse> listarTodos(Pageable pageable) {
        return tanqueRepository.findAll(pageable).map(this::toResponse);
    }

    public List<TanqueResponse> listarPorFazenda(Long fazendaId) {
        return tanqueRepository.findByFazendaId(fazendaId)
                .stream().map(this::toResponse).toList();
    }

    public TanqueResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public TanqueResponse atualizar(Long id, AtualizarTanqueRequest request) {
        Tanque tanque = buscarEntidade(id);

        if (request.tipoAlga() != null) tanque.setTipoAlga(request.tipoAlga());
        if (request.capacidadeLitros() != null) tanque.setCapacidadeLitros(request.capacidadeLitros());
        if (request.phMin() != null) tanque.setPhMin(request.phMin());
        if (request.phMax() != null) tanque.setPhMax(request.phMax());
        if (request.temperaturaMin() != null) tanque.setTemperaturaMin(request.temperaturaMin());
        if (request.temperaturaMax() != null) tanque.setTemperaturaMax(request.temperaturaMax());
        if (request.status() != null) tanque.setStatus(request.status());

        return toResponse(tanqueRepository.save(tanque));
    }

    @Transactional
    public void deletar(Long id) {
        if (!tanqueRepository.existsById(id)) {
            throw new EntityNotFoundException("Tanque não encontrado: " + id);
        }
        tanqueRepository.deleteById(id);
    }

    private Tanque buscarEntidade(Long id) {
        return tanqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado: " + id));
    }

    public TanqueResponse toResponse(Tanque t) {
        return new TanqueResponse(
                t.getId(), t.getFazenda().getId(), t.getFazenda().getNome(),
                t.getCodigoTanque(), t.getTipoAlga(), t.getCapacidadeLitros(),
                t.getPhMin(), t.getPhMax(), t.getTemperaturaMin(),
                t.getTemperaturaMax(), t.getStatus(), t.getDtInstalacao()
        );
    }
}
