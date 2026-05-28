package br.com.fiap.aquaorbital.repository;

import br.com.fiap.aquaorbital.entity.TransacaoMarketplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransacaoMarketplaceRepository extends JpaRepository<TransacaoMarketplace, Long> {
    List<TransacaoMarketplace> findByUsuarioCompradorId(Long usuarioId);
    List<TransacaoMarketplace> findByStatus(String status);
    List<TransacaoMarketplace> findByTipoTransacao(String tipoTransacao);
    List<TransacaoMarketplace> findByLoteId(Long loteId);
    List<TransacaoMarketplace> findByCreditoId(Long creditoId);
}