package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.FaixaReferencia;
import com.senai.monsai.domain.entity.MensagemMqtt;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.DispositivoRepository;
import com.senai.monsai.domain.repository.FaixaReferenciaRepository;
import com.senai.monsai.domain.repository.MensagemMqttRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelemetriaService {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private MensagemMqttRepository mensagemRepository;

    @Autowired
    private FaixaReferenciaRepository faixaRepository; // Injetando as regras


    // Se você criar um repositório para salvar os alertas no banco, injete-o aqui:
    // @Autowired
    // private AlertaRepository alertaRepository;

    @Transactional
    public void processarTelemetria(TelemetriaDTO dto) {
        // 1. Buscar o dispositivo
        Dispositivo dispositivo = dispositivoRepository.findBySerial(dto.pulseiraId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta: Dispositivo " + dto.pulseiraId() + " não cadastrado!"));

        // 2. Validação de Segurança (Cross-Tenant/Cross-Patient Leak)
        if (!dispositivo.getIdoso().getId().equals(dto.idosoId())) {
            System.err.println("ALERTA DE SEGURANÇA GRAVE: Pulseira " + dto.pulseiraId() +
                    " reportando dados para o idoso errado (" + dto.idosoId() + ")");
            // O ideal aqui é lançar uma exceção de segurança para barrar o processamento!
            throw new SecurityException("Incompatibilidade entre dispositivo e paciente.");
        }

        // 3. Atualizar o Estado do Dispositivo
        dispositivo.setNivelBateria(dto.statusDoDispositivo().nivelBateria());
        dispositivo.setStatusDispositivo(dto.statusDoDispositivo().statusDispositivo());
        dispositivo.setUltimoContato(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        // 4. Criar o Log Histórico (AACI-262 - Persistência Contínua)
        MensagemMqtt historico = new MensagemMqtt();
        historico.setDispositivo(dispositivo);
        historico.setFrequenciaCardiaca(dto.sinalVital().frequenciaCardiacaBpm());
        historico.setTemperatura(dto.sinalVital().temperaturaC());
        historico.setQuedaDetectada(dto.sinalVital().movimento().quedaDetectada());
        historico.setLatitude(dto.localizacao().latitude());
        historico.setLongitude(dto.localizacao().longitude());
        historico.setDataHoraEvento(LocalDateTime.parse(dto.dataHora(), DateTimeFormatter.ISO_DATE_TIME));
        historico.setDataRecebimento(LocalDateTime.now());

        mensagemRepository.save(historico);

        // 5. NOVA ETAPA: Analisar dados clínicos e disparar alertas
        analisarSinaisEGerarAlertas(dto, dispositivo);

        System.out.println("Telemetria processada com sucesso para o Idoso ID: " + dto.idosoId());
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
        int minBpm = (faixa != null) ? faixa.getMinBpm() : 60;
        int maxBpm = (faixa != null) ? faixa.getMaxBpm() : 100;

        if (bpm < minBpm || bpm > maxBpm) {
            motivosAlerta.add("⚠️ ANOMALIA CARDÍACA: BPM " + bpm + " (Fora da faixa: " + minBpm + "-" + maxBpm + ")");
        }

        double temp = dto.sinalVital().temperaturaC();
        double minT = (faixa != null) ? faixa.getMinTemp() : 35.5;
        double maxT = (faixa != null) ? faixa.getMaxTemp() : 37.5;

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
        // Aqui você pode salvar o alerta no banco de dados
        // Alerta novoAlerta = new Alerta(dispositivo.getIdoso(), motivos);
        // alertaRepository.save(novoAlerta);

        System.out.println("\n=================================================");
        System.out.println("🔔 INICIANDO PROTOCOLO DE ALERTA PARA O IDOSO ID: " + dispositivo.getIdoso().getId());

        for (String motivo : motivos) {
            System.out.println(motivo);
        }

        // Como você fez o vínculo no UsuarioController, o Idoso deve ter uma lista de usuários responsáveis:
        /*
        List<Usuario> cuidadores = dispositivo.getIdoso().getUsuariosVinculados();
        if (cuidadores != null && !cuidadores.isEmpty()) {
            for (Usuario cuidador : cuidadores) {
                System.out.println("-> Enviando Push Notification / WebSocket / Email para: " + cuidador.getEmail());
                // pushNotificationService.enviar(cuidador.getTokenDispositivo(), motivos);
            }
        } else {
            System.err.println("-> ATENÇÃO: Nenhum cuidador ou familiar vinculado a este idoso para receber o alerta!");
        }
        */
        System.out.println("=================================================\n");
    }
}