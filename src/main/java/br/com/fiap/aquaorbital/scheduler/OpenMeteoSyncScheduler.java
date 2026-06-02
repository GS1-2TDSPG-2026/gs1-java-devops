package br.com.fiap.aquaorbital.scheduler;

import br.com.fiap.aquaorbital.repository.FazendaRepository;
import br.com.fiap.aquaorbital.service.OpenMeteoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMeteoSyncScheduler {

    private final OpenMeteoService  openMeteoService;
    private final FazendaRepository fazendaRepository;


    @Scheduled(cron = "0 0/15 * * * *")
    public void sincronizarTodasFazendas() {
        log.info("[ Open-Meteo ] Iniciando sincronização em tempo real...");

        fazendaRepository.findAll().forEach(fazenda -> {
            try {
                openMeteoService.buscarESalvarDadosOpenMeteo(fazenda.getId());
                log.debug("[ Open-Meteo ] fazenda={} sincronizada.", fazenda.getId());
            } catch (Exception e) {
                log.error("[ Open-Meteo ] Erro na fazenda={} | motivo={}",
                        fazenda.getId(), e.getMessage());
            }
        });

        log.info("[ Open-Meteo ] Sincronização concluída.");
    }
}