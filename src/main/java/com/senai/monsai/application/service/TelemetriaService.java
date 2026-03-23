package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.MensagemMqtt;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.DispositivoRepository;
import com.senai.monsai.domain.repository.MensagemMqttRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TelemetriaService {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private MensagemMqttRepository mensagemRepository;

    @Transactional
    public void processarTelemetria(TelemetriaDTO dto) {
        // 1. Buscar o dispositivo (Tiramos a RuntimeException genérica daqui também!)
        Dispositivo dispositivo = dispositivoRepository.findById(dto.pulseiraId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta: Dispositivo " + dto.pulseiraId() + " não cadastrado!"));

        // 2. Validação de Segurança
        // Verifica se a pulseira pertence ao idoso que ela diz estar monitorando
        // OLHA A MÁGICA AQUI: Trocamos .getIdoso_id() por .getId()
        if (!dispositivo.getIdoso().getId().equals(dto.idosoId())) {
            System.err.println("ALERTA DE SEGURANÇA: Dados do dispositivo " + dto.pulseiraId() +
                    " não pertencem ao idoso " + dto.idosoId());
            return;
        }

        // 3. Atualizar o Estado do Dispositivo
        dispositivo.setNivelBateria(dto.statusDoDispositivo().nivelBateria());
        dispositivo.setStatusDispositivo(dto.statusDoDispositivo().statusDispositivo());
        dispositivo.setUltimoContato(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        // 4. Criar o Log Histórico (A Mensagem)
        MensagemMqtt historico = new MensagemMqtt();
        historico.setDispositivo(dispositivo);

        // Sinais Vitais
        historico.setFrequenciaCardiaca(dto.sinalVital().frequenciaCardiacaBpm());
        historico.setTemperatura(dto.sinalVital().temperaturaC());
        historico.setQuedaDetectada(dto.sinalVital().movimento().quedaDetectada());

        // Localização
        historico.setLatitude(dto.localizacao().latitude());
        historico.setLongitude(dto.localizacao().longitude());

        // Datas
        historico.setDataHoraEvento(LocalDateTime.parse(dto.dataHora(), DateTimeFormatter.ISO_DATE_TIME));
        historico.setDataRecebimento(LocalDateTime.now());

        mensagemRepository.save(historico);

        System.out.println("Telemetria processada com sucesso para o Idoso: " + dto.idosoId());
    }
}