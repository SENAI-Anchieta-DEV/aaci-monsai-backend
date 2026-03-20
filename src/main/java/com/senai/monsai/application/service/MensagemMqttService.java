package com.senai.monsai.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.senai.monsai.application.dto.MensagemMqttDTO;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.MensagemMqtt;
import com.senai.monsai.domain.repository.MensagemMqttRepository;
import com.senai.monsai.infrastructure.config.MqttGateway;
import com.senai.monsai.ui_interface.controller.TelemetriaController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MensagemMqttService {
    private final TelemetriaService telemetriaService;
    private final TelemetriaController telemetriaController;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void escutarHardware(Message<String> message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 🟢 Adicione isso para o Jackson entender as datas do Java 8
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            TelemetriaDTO dto = mapper.readValue(message.getPayload(), TelemetriaDTO.class);

            // 1. Salva no Banco (Histórico)
            telemetriaService.processarTelemetria(dto);

            // 2. Atualiza a "Ultima Telemetria" para o React ver (Tempo Real)
            telemetriaController.atualizarDados(dto);

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
