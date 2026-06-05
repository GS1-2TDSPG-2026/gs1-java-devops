package br.com.fiap.Phycocarbon.service;

import br.com.fiap.Phycocarbon.dto.FazendaDTOs.AtualizarFazendaRequest;
import br.com.fiap.Phycocarbon.dto.FazendaDTOs.CriarFazendaRequest;
import br.com.fiap.Phycocarbon.dto.ResponseDTOs.*;
import br.com.fiap.Phycocarbon.entity.CreditoCarbono;
import br.com.fiap.Phycocarbon.entity.Fazenda;
import br.com.fiap.Phycocarbon.entity.Usuario;
import br.com.fiap.Phycocarbon.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FazendaService {

    private final FazendaRepository fazendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TanqueRepository tanqueRepository;
    private final CreditoCarbonoRepository creditoCarbonoRepository;
    private final LoteBiomassaRepository loteBiomassaRepository;
    private final DadoOrbitalRepository dadoOrbitalRepository;

    @Transactional
    public FazendaResponse criar(CriarFazendaRequest request, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        Fazenda fazenda = Fazenda.builder()
                .nome(request.nome())
                .cidade(request.cidade())
                .uf(request.uf())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .usuarioResponsavel(usuario)
                .build();
        return toResponse(fazendaRepository.save(fazenda));
    }


    @Transactional(readOnly = true)
    public Page<FazendaResponse> listarTodas(Pageable pageable) {
        return fazendaRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<FazendaResponse> listarPorUsuario(Long usuarioId) {
        return fazendaRepository.findByUsuarioResponsavelId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FazendaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public FazendaResponse atualizar(Long id, AtualizarFazendaRequest request) {
        Fazenda fazenda = buscarEntidade(id);
        if (request.nome() != null)      fazenda.setNome(request.nome());
        if (request.cidade() != null)    fazenda.setCidade(request.cidade());
        if (request.uf() != null)        fazenda.setUf(request.uf());
        if (request.latitude() != null)  fazenda.setLatitude(request.latitude());
        if (request.longitude() != null) fazenda.setLongitude(request.longitude());
        if (request.status() != null)    fazenda.setStatus(request.status());
        return toResponse(fazendaRepository.save(fazenda));
    }

    @Transactional
    public void deletar(Long id) {
        if (!fazendaRepository.existsById(id))
            throw new EntityNotFoundException("Fazenda não encontrada: " + id);
        fazendaRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public DashboardFazendaResponse getDashboard(Long idFazenda) {
        Fazenda fazenda = buscarEntidade(idFazenda);

        int totalTanques = tanqueRepository.findByFazendaId(idFazenda).size();
        int tanquesAtivos = tanqueRepository.findByStatus("ATIVO").stream()
                .filter(t -> t.getFazenda().getId().equals(idFazenda))
                .toList().size();
        int lotesDisponiveis = loteBiomassaRepository.findByStatus("DISPONIVEL").stream()
                .filter(l -> l.getFazenda().getId().equals(idFazenda))
                .toList().size();

        List<CreditoCarbono> creditos = creditoCarbonoRepository.findByFazendaId(idFazenda);
        int creditosDisponiveis = (int) creditos.stream()
                .filter(c -> "DISPONIVEL".equals(c.getStatus()))
                .count();
        BigDecimal totalCo2 = creditos.stream()
                .map(CreditoCarbono::getCo2Toneladas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DadoOrbitalResponse ultimoDadoOrbital = dadoOrbitalRepository
                .findTopByFazendaIdOrderByDtColetaDesc(idFazenda)
                .map(d -> new DadoOrbitalResponse(
                        d.getId(), d.getFazenda().getId(), d.getFazenda().getNome(),
                        d.getFonte(), d.getDtColeta(), d.getIrradianciaParTot(),
                        d.getNebulosidade(), d.getTemperaturaAmbiente(),
                        d.getLatitude(), d.getLongitude(), d.getDtRegistro()))
                .orElse(null);

        return new DashboardFazendaResponse(
                idFazenda, fazenda.getNome(), totalTanques, tanquesAtivos,
                lotesDisponiveis, creditosDisponiveis, totalCo2, ultimoDadoOrbital);
    }

    private Fazenda buscarEntidade(Long id) {
        return fazendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fazenda não encontrada: " + id));
    }

    private FazendaResponse toResponse(Fazenda f) {
        return new FazendaResponse(
                f.getId(),
                f.getNome(),
                f.getCidade(),
                f.getUf(),
                f.getLatitude(),
                f.getLongitude(),
                f.getStatus(),
                f.getDtCadastro(),
                f.getUsuarioResponsavel().getId(),
                f.getUsuarioResponsavel().getNome()
        );
    }
}