package com.senai.monsai.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.senai.monsai.application.dto.MensagemMqttDTO;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.MensagemMqtt;
import com.senai.monsai.domain.repository.MensagemMqttRepository;
import com.senai.monsai.infrastructure.config.MqttGateway;
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

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void escutarHardware(Message<String> message) {
        try {
            // Instanciamos o mapper aqui direto para evitar qualquer erro do Spring
            ObjectMapper mapper = new ObjectMapper();

            // Pega o JSON que chegou do MQTT (hardware) e converte pro DTO
            TelemetriaDTO dto = mapper.readValue(message.getPayload(), TelemetriaDTO.class);

            // Manda pro Service principal fazer o trabalho pesado (validar e salvar)
            telemetriaService.processarTelemetria(dto);

        } catch (Exception e) {
            System.err.println("Falha ao processar mensagem do hardware: " + e.getMessage());
        }
    }
}
