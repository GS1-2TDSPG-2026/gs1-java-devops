package br.com.fiap.Phycocarbon.repository;

import br.com.fiap.Phycocarbon.entity.LoteBiomassa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoteBiomassaRepository extends JpaRepository<LoteBiomassa, Long> {
    List<LoteBiomassa> findByFazendaId(Long fazendaId);
    List<LoteBiomassa> findByStatus(String status);
    List<LoteBiomassa> findByTanqueId(Long tanqueId);
    List<LoteBiomassa> findByTaxonomiaAlga(String taxonomiaAlga);
}
