package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.AlertaDTO;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.FaixaReferencia;
import com.senai.monsai.domain.entity.MensagemMqtt;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.DispositivoRepository;
import com.senai.monsai.domain.repository.FaixaReferenciaRepository;
import com.senai.monsai.domain.repository.MensagemMqttRepository;
import com.senai.monsai.ui_interface.controller.TelemetriaController;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TelemetriaService {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private MensagemMqttRepository mensagemRepository;

    @Autowired
    private FaixaReferenciaRepository faixaRepository; // Injetando as regras

    // Lista na memória para guardar os alertas "vivos"
    public static final List<AlertaDTO> ALERTA_CACHE = new CopyOnWriteArrayList<>();

    // 2. MÉTODO PARA O CONTROLLER PEGAR OS DADOS
    @Getter
    private final Map<String, TelemetriaDTO> ultimasTelemetrias = new ConcurrentHashMap<>();

    @Transactional
    public void processarTelemetria(TelemetriaDTO dto) {
        // 1. Buscar o dispositivo
        Dispositivo dispositivo = dispositivoRepository.findBySerial(dto.pulseiraId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta: Dispositivo " + dto.pulseiraId() + " não cadastrado!"));

        // 2. Valida segurança
        if (!dispositivo.getIdoso().getId().equals(dto.idosoId())) {
            throw new SecurityException("Incompatibilidade entre dispositivo e paciente.");
        }

        // 3. Salva no banco o histórico (O seu código original)
        dispositivo.setNivelBateria(dto.statusDoDispositivo().nivelBateria());
        dispositivo.setUltimoContato(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        MensagemMqtt historico = new MensagemMqtt();
        historico.setDispositivo(dispositivo);
        historico.setFrequenciaCardiaca(dto.sinalVital().frequenciaCardiacaBpm());
        historico.setTemperatura(dto.sinalVital().temperaturaC());
        historico.setQuedaDetectada(dto.sinalVital().movimento().quedaDetectada());
        historico.setDataRecebimento(LocalDateTime.now());
        // (adicione os outros campos de latitude/longitude se precisar)
        mensagemRepository.save(historico);

        // 4. Analisa sinais e gera alertas
        analisarSinaisEGerarAlertas(dto, dispositivo);

        // 5. ATUALIZA O ESTADO EM MEMÓRIA PARA O REACT!
        ultimasTelemetrias.put(dto.pulseiraId(), dto);

        System.out.println("Telemetria processada e salva com sucesso para o Idoso ID: " + dto.idosoId());
    }

    /**
     * Motor de Regras para Alertas Clínicos e de Sistema
     */
    private void analisarSinaisEGerarAlertas(TelemetriaDTO dto, Dispositivo dispositivo) {
        List<String> motivosAlerta = new ArrayList<>();
        Long idosoId = dispositivo.getIdoso().getId();

        // Buscamos a faixa personalizada do Idoso no banco
        FaixaReferencia faixa = faixaRepository.findByIdosoId(idosoId)
                .orElse(null);

        // Regra 1: Queda (Igual ao seu)
        if (dto.sinalVital().movimento().quedaDetectada()) {
            motivosAlerta.add("🚨 CRÍTICO: Queda detectada!");
        }

        // Se houver faixa cadastrada, usamos os limites dela. Se não, usamos o padrão (Fallback)
        int bpm = dto.sinalVital().frequenciaCardiacaBpm();
        int minBpm = (faixa != null && faixa.getMinBpm() != null) ? faixa.getMinBpm() : 60;
        int maxBpm = (faixa != null && faixa.getMaxBpm() != null) ? faixa.getMaxBpm() : 100;

        if (bpm < minBpm || bpm > maxBpm) {
            motivosAlerta.add("⚠️ ANOMALIA CARDÍACA: BPM " + bpm + " (Fora da faixa: " + minBpm + "-" + maxBpm + ")");
        }

        double temp = dto.sinalVital().temperaturaC();
        double minT = (faixa != null && faixa.getMinTemp() != null) ? faixa.getMinTemp() : 35.5;
        double maxT = (faixa != null && faixa.getMaxTemp() != null) ? faixa.getMaxTemp() : 37.5;

        if (temp < minT || temp > maxT) {
            motivosAlerta.add("⚠️ TEMPERATURA ANORMAL: " + temp + " °C (Limite: " + minT + "-" + maxT + ")");
        }

        // Bateria (Regra de sistema, continua igual)
        if (dto.statusDoDispositivo().nivelBateria() <= 15) {
            motivosAlerta.add("🔋 BATERIA FRACA");
        }

        if (!motivosAlerta.isEmpty()) {
            dispararNotificacoes(dispositivo, motivosAlerta);
        }
    }

    /**
     * Simula o envio do alerta para os usuários vinculados ao Idoso
     */
    private void dispararNotificacoes(Dispositivo dispositivo, List<String> motivos) {
        System.out.println("\n=================================================");
        System.out.println("🔔 INICIANDO PROTOCOLO DE ALERTA PARA O IDOSO ID: " + dispositivo.getIdoso().getId());
        for (String motivo : motivos) {
            System.out.println(motivo);
        }
        System.out.println("=================================================\n");

        // 2. ADICIONE ESTE BLOCO: Transforma o alerta num DTO e guarda na lista!
        AlertaDTO novoAlerta = new AlertaDTO(
                UUID.randomUUID().toString(),
                dispositivo.getIdoso().getId(),
                dispositivo.getIdoso().getNome(),
                motivos,
                LocalDateTime.now().toString(),
                false // visto = false (É um alerta novo!)
        );

        ALERTA_CACHE.add(0, novoAlerta); // Adiciona no topo da lista (mais recente primeiro)

        // Limpa alertas muito antigos para não encher a memória do PC
        if(ALERTA_CACHE.size() > 50) {
            ALERTA_CACHE.remove(ALERTA_CACHE.size() - 1);
        }
    }
}