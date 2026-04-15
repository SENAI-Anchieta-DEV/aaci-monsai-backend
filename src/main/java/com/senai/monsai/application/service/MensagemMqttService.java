package com.senai.monsai.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.ui_interface.controller.TelemetriaController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- Import do Logger
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MensagemMqttService {

    private final TelemetriaService telemetriaService;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void escutarHardware(Message<String> message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            TelemetriaDTO dto = mapper.readValue(message.getPayload(), TelemetriaDTO.class);

            // 1. Salva no Banco e gera Alertas
            telemetriaService.processarTelemetria(dto);

            // 2. Atualiza a "Ultima Telemetria" para o React
          //  TelemetriaController.atualizarDados(dto);

        } catch (JsonProcessingException e) {
            // Erro 1: A pulseira mandou um JSON mal formatado
            log.error("[MQTT ERRO] Payload inválido ou mal formatado: {}. Erro: {}", message.getPayload(), e.getMessage());
        } catch (SecurityException e) {
            // Erro 2: Cross-Tenant Leak (Pulseira no idoso errado)
            log.error("[MQTT SEGURANÇA] Tentativa de violação detectada: {}", e.getMessage());
        } catch (Exception e) {
            // Erro 3: Qualquer outro erro genérico (Banco de dados fora, etc)
            log.error("[MQTT FATAL] Falha inesperada ao processar telemetria: ", e);
        }
    }
}