package br.com.fiap.aquaorbital.repository;

import br.com.fiap.aquaorbital.entity.BrDadoOrbital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrDadoOrbitalRepository extends JpaRepository<BrDadoOrbital, Long> {

    boolean existsByCodEstacaoAndDataMedicaoAndHoraMedicao(
            String codEstacao, String dataMedicao, String horaMedicao);

    List<BrDadoOrbital> findByCodEstacaoAndDataMedicao(String codEstacao, String dataMedicao);

    List<BrDadoOrbital> findByCodEstacaoOrderByDataMedicaoDescHoraMedicaoDesc(
            String codEstacao);

    List<BrDadoOrbital> findByCodEstacaoAndDataMedicaoBetweenOrderByDataMedicaoDescHoraMedicaoDesc(
            String codEstacao, String inicio, String fim);

    Optional<BrDadoOrbital> findFirstByCodEstacaoOrderByDataMedicaoDescHoraMedicaoDesc(
            String codEstacao);

    @Query("SELECT DISTINCT b.codEstacao FROM BrDadoOrbital b ORDER BY b.codEstacao")
    List<String> findEstacoesDistintas();
}