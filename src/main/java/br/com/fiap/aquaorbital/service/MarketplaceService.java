package br.com.fiap.aquaorbital.service;

import br.com.fiap.aquaorbital.dto.MarketplaceDTOs.*;
import br.com.fiap.aquaorbital.dto.ResponseDTOs.*;
import br.com.fiap.aquaorbital.entity.*;
import br.com.fiap.aquaorbital.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final LoteBiomassaRepository loteRepository;
    private final CreditoCarbonoRepository creditoRepository;
    private final TransacaoMarketplaceRepository transacaoRepository;
    private final FazendaRepository fazendaRepository;
    private final TanqueRepository tanqueRepository;
    private final UsuarioRepository usuarioRepository;



    @Transactional
    public LoteBiomassaResponse criarLote(CriarLoteRequest request) {
        Fazenda fazenda = fazendaRepository.findById(request.idFazenda())
                .orElseThrow(() -> new EntityNotFoundException("Fazenda não encontrada"));
        Tanque tanque = tanqueRepository.findById(request.idTanque())
                .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado"));

        LoteBiomassa lote = LoteBiomassa.builder()
                .fazenda(fazenda)
                .tanque(tanque)
                .taxonomiaAlga(request.taxonomiaAlga())
                .pesoKg(request.pesoKg())
                .precoUnitario(request.precoUnitario())
                .build();

        return toLoteResponse(loteRepository.save(lote));
    }

    public Page<LoteBiomassaResponse> listarLotes(Pageable pageable) {
        return loteRepository.findAll(pageable).map(this::toLoteResponse);
    }

    public List<LoteBiomassaResponse> listarLotesDisponiveis() {
        return loteRepository.findByStatus("DISPONIVEL")
                .stream().map(this::toLoteResponse).toList();
    }

    public LoteBiomassaResponse buscarLote(Long id) {
        return toLoteResponse(buscarLoteEntidade(id));
    }

    @Transactional
    public LoteBiomassaResponse atualizarStatusLote(Long id, AtualizarStatusLoteRequest request) {
        LoteBiomassa lote = buscarLoteEntidade(id);
        if ("VENDIDO".equals(lote.getStatus())) {
            throw new IllegalArgumentException("Lote já foi vendido e não pode ser alterado");
        }
        lote.setStatus(request.status());
        return toLoteResponse(loteRepository.save(lote));
    }

    @Transactional
    public void deletarLote(Long id) {
        LoteBiomassa lote = buscarLoteEntidade(id);
        if ("VENDIDO".equals(lote.getStatus())) {
            throw new IllegalArgumentException("Lote vendido não pode ser removido");
        }
        loteRepository.deleteById(id);
    }



    public Page<CreditoCarbonoResponse> listarCreditos(Pageable pageable) {
        return creditoRepository.findAll(pageable).map(this::toCreditoResponse);
    }

    public List<CreditoCarbonoResponse> listarCreditosPorFazenda(Long fazendaId) {
        return creditoRepository.findByFazendaId(fazendaId)
                .stream().map(this::toCreditoResponse).toList();
    }

    public CreditoCarbonoResponse buscarCredito(Long id) {
        return toCreditoResponse(buscarCreditoEntidade(id));
    }

    @Transactional
    public CreditoCarbonoResponse validarCredito(Long id) {
        CreditoCarbono credito = buscarCreditoEntidade(id);
        credito.setStatus("VALIDADO");
        credito.setDtValidacao(LocalDateTime.now());
        return toCreditoResponse(creditoRepository.save(credito));
    }



    @Transactional
    public TransacaoResponse criarTransacao(CriarTransacaoRequest request, Long usuarioId) {
        Usuario comprador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        LoteBiomassa lote = null;
        CreditoCarbono credito = null;

        if ("COMPRA_BIOMASSA".equals(request.tipoTransacao())) {
            if (request.idLote() == null)
                throw new IllegalArgumentException("idLote é obrigatório para COMPRA_BIOMASSA");
            lote = buscarLoteEntidade(request.idLote());
            if (!"DISPONIVEL".equals(lote.getStatus()))
                throw new IllegalArgumentException("Lote não está disponível para compra");
            lote.setStatus("VENDIDO");
            loteRepository.save(lote);
        } else if ("COMPRA_CREDITO_CARBONO".equals(request.tipoTransacao())) {
            if (request.idCredito() == null)
                throw new IllegalArgumentException("idCredito é obrigatório para COMPRA_CREDITO_CARBONO");
            credito = buscarCreditoEntidade(request.idCredito());
            if (!"DISPONIVEL".equals(credito.getStatus()))
                throw new IllegalArgumentException("Crédito não está disponível para compra");
            credito.setStatus("VENDIDO");
            creditoRepository.save(credito);
        } else if (!"VENDA_BIOMASSA".equals(request.tipoTransacao())) {
            throw new IllegalArgumentException("Tipo de transação inválido: " + request.tipoTransacao());
        }

        TransacaoMarketplace transacao = TransacaoMarketplace.builder()
                .usuarioComprador(comprador)
                .lote(lote)
                .credito(credito)
                .tipoTransacao(request.tipoTransacao())
                .quantidade(request.quantidade())
                .valorTotal(request.valorTotal())
                .status("CONFIRMADA") // corrigido: era "CONCLUIDA", divergia do padrao do banco
                .build();

        return toTransacaoResponse(transacaoRepository.save(transacao));
    }

    public Page<TransacaoResponse> listarTransacoes(Pageable pageable) {
        return transacaoRepository.findAll(pageable).map(this::toTransacaoResponse);
    }

    public List<TransacaoResponse> listarTransacoesPorUsuario(Long usuarioId) {
        return transacaoRepository.findByUsuarioCompradorId(usuarioId)
                .stream().map(this::toTransacaoResponse).toList();
    }



    public static String gerarHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }

    private LoteBiomassa buscarLoteEntidade(Long id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lote não encontrado: " + id));
    }

    private CreditoCarbono buscarCreditoEntidade(Long id) {
        return creditoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crédito não encontrado: " + id));
    }

    private LoteBiomassaResponse toLoteResponse(LoteBiomassa l) {
        return new LoteBiomassaResponse(
                l.getId(), l.getFazenda().getId(), l.getFazenda().getNome(),
                l.getTanque().getId(), l.getTanque().getCodigoTanque(),
                l.getTaxonomiaAlga(), l.getPesoKg(), l.getPrecoUnitario(),
                l.getStatus(), l.getDtColheita()
        );
    }

    private CreditoCarbonoResponse toCreditoResponse(CreditoCarbono c) {
        return new CreditoCarbonoResponse(
                c.getId(), c.getFazenda().getId(), c.getFazenda().getNome(),
                c.getLote().getId(), c.getCo2Toneladas(), c.getHashAuditoria(),
                c.getStatus(), c.getDtValidacao()
        );
    }

    private TransacaoResponse toTransacaoResponse(TransacaoMarketplace t) {
        return new TransacaoResponse(
                t.getId(), t.getUsuarioComprador().getId(), t.getUsuarioComprador().getNome(),
                t.getLote() != null ? t.getLote().getId() : null,
                t.getCredito() != null ? t.getCredito().getId() : null,
                t.getTipoTransacao(), t.getQuantidade(), t.getValorTotal(),
                t.getStatus(), t.getDtTransacao()
        );
    }
}
