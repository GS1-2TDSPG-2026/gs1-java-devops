package br.com.fiap.aquaorbital.controller;

import br.com.fiap.aquaorbital.dto.MarketplaceDTOs.*;
import br.com.fiap.aquaorbital.dto.ResponseDTOs.*;
import br.com.fiap.aquaorbital.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Marketplace", description = "Compra e venda de biomassa e créditos de carbono")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    // ── LOTES ──────────────────────────────────────────────────────────────

    @PostMapping("/lotes")
    @Operation(summary = "Publicar novo lote de biomassa")
    public ResponseEntity<LoteBiomassaResponse> criarLote(@Valid @RequestBody CriarLoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketplaceService.criarLote(request));
    }

    @GetMapping("/lotes")
    @Operation(summary = "Listar todos os lotes (paginado)")
    public ResponseEntity<Page<LoteBiomassaResponse>> listarLotes(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(marketplaceService.listarLotes(pageable));
    }

    @GetMapping("/lotes/disponiveis")
    @Operation(summary = "Listar lotes disponíveis para compra")
    public ResponseEntity<List<LoteBiomassaResponse>> listarLotesDisponiveis() {
        return ResponseEntity.ok(marketplaceService.listarLotesDisponiveis());
    }

    @GetMapping("/lotes/{id}")
    @Operation(summary = "Buscar lote por ID")
    public ResponseEntity<LoteBiomassaResponse> buscarLote(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.buscarLote(id));
    }

    @PatchMapping("/lotes/{id}/status")
    @Operation(summary = "Atualizar status do lote")
    public ResponseEntity<LoteBiomassaResponse> atualizarStatusLote(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusLoteRequest request) {
        return ResponseEntity.ok(marketplaceService.atualizarStatusLote(id, request));
    }

    @DeleteMapping("/lotes/{id}")
    @Operation(summary = "Remover lote")
    public ResponseEntity<Void> deletarLote(@PathVariable Long id) {
        marketplaceService.deletarLote(id);
        return ResponseEntity.noContent().build();
    }

    // ── CRÉDITOS DE CARBONO ────────────────────────────────────────────────

    @GetMapping("/creditos")
    @Operation(summary = "Listar todos os créditos de carbono (paginado)")
    public ResponseEntity<Page<CreditoCarbonoResponse>> listarCreditos(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(marketplaceService.listarCreditos(pageable));
    }

    @GetMapping("/creditos/fazenda/{fazendaId}")
    @Operation(summary = "Extrato de CO₂ por fazenda")
    public ResponseEntity<List<CreditoCarbonoResponse>> extratoCarbono(@PathVariable Long fazendaId) {
        return ResponseEntity.ok(marketplaceService.listarCreditosPorFazenda(fazendaId));
    }

    @GetMapping("/creditos/{id}")
    @Operation(summary = "Buscar crédito de carbono por ID")
    public ResponseEntity<CreditoCarbonoResponse> buscarCredito(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.buscarCredito(id));
    }

    @PatchMapping("/creditos/{id}/validar")
    @Operation(summary = "Validar crédito de carbono")
    public ResponseEntity<CreditoCarbonoResponse> validarCredito(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.validarCredito(id));
    }

    // ── TRANSAÇÕES ─────────────────────────────────────────────────────────

    @PostMapping("/transacoes")
    @Operation(summary = "Realizar transação de compra")
    public ResponseEntity<TransacaoResponse> criarTransacao(
            @Valid @RequestBody CriarTransacaoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = ((br.com.fiap.aquaorbital.entity.Usuario) userDetails).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marketplaceService.criarTransacao(request, usuarioId));
    }

    @GetMapping("/transacoes")
    @Operation(summary = "Listar todas as transações (paginado)")
    public ResponseEntity<Page<TransacaoResponse>> listarTransacoes(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(marketplaceService.listarTransacoes(pageable));
    }

    @GetMapping("/transacoes/minhas")
    @Operation(summary = "Listar minhas transações")
    public ResponseEntity<List<TransacaoResponse>> minhasTransacoes(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = ((br.com.fiap.aquaorbital.entity.Usuario) userDetails).getId();
        return ResponseEntity.ok(marketplaceService.listarTransacoesPorUsuario(usuarioId));
    }
}