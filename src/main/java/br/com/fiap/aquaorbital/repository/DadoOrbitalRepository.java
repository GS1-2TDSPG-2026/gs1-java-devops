package br.com.fiap.aquaorbital.repository;

import br.com.fiap.aquaorbital.entity.DadoOrbital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DadoOrbitalRepository extends JpaRepository<DadoOrbital, Long> {
    List<DadoOrbital> findByFazendaIdOrderByDtColetaDesc(Long fazendaId);
    Optional<DadoOrbital> findByFazendaIdAndDtColeta(Long fazendaId, LocalDate dtColeta);
    List<DadoOrbital> findByFazendaIdAndDtColetaBetweenOrderByDtColetaDesc(Long fazendaId, LocalDate inicio, LocalDate fim);
    Optional<DadoOrbital> findTopByFazendaIdOrderByDtColetaDesc(Long fazendaId);
}
