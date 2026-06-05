package br.com.fiap.Phycocarbon.scheduler;

import br.com.fiap.Phycocarbon.repository.FazendaRepository;
import br.com.fiap.Phycocarbon.service.DadoOrbitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NasaSyncScheduler {

    private final DadoOrbitalService dadoOrbitalService;
    private final FazendaRepository fazendaRepository;

    @Scheduled(cron = "0 0 6 * * *")
    public void sincronizarTodasFazendas() {
        log.info("Iniciando sincronização automática com NASA POWER...");
        fazendaRepository.findAll().forEach(fazenda -> {
            try {
                dadoOrbitalService.buscarEsalvarDadosNasa(fazenda.getId());
            } catch (Exception e) {
                log.error("Erro ao sincronizar fazenda={} | motivo={}", fazenda.getId(), e.getMessage());
            }
        });
        log.info("Sincronização automática concluída.");
    }
}