package br.com.fiap.aquaorbital.repository;


import br.com.fiap.aquaorbital.entity.BrDadoOrbital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrDadoOrbitalRepository extends JpaRepository<BrDadoOrbital, Long> {


    boolean existsByCodEstacaoAndDataMedicaoAndHoraMedicao(
            String codEstacao, String dataMedicao, String horaMedicao);

    List<BrDadoOrbital> findByCodEstacaoAndDataMedicao(String codEstacao, String dataMedicao);
}