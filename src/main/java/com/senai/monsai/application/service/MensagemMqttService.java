package com.senai.monsai.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.senai.monsai.application.dto.MensagemMqttDTO;
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

    @Autowired
    private final MensagemMqttRepository repository;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void processarRecepcao(Message<String> message) {
        String topico = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = message.getPayload();

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            // Converte o JSON para o novo DTO
            MensagemMqttDTO dto = mapper.readValue(payload, MensagemMqttDTO.class);

            // Mapeia para a entidade
            MensagemMqtt novaMensagem = MensagemMqtt.builder()
                    .topico(topico)
                    .conteudo("Dispositivo: " + dto.dispositivoId() + " | Valor: " + dto.valor())
                    .dataRecebimento(LocalDateTime.now()) // Gerado pelo servidor, evitando erro de parsing
                    .build();

            repository.save(novaMensagem);
            System.out.println("✅ Mensagem válida salva no banco.");
        } catch (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException e) {
            System.err.println("❌ Erro de Contrato: Campo inesperado recebido: " + e.getPropertyName());
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado no processamento: " + e.getMessage());
        }
    }

    @Autowired
    private MqttGateway mqttGateway; // Aquela interface que criamos na MqttConfig

    public void publicar(String topico, String conteudo) {
        mqttGateway.sendToMqtt(conteudo, topico);
        System.out.println("Enviando para o Broker - Tópico: " + topico + " | Mensagem: " + conteudo);
    }

    // AACI-136: Implementar ação simples
    public void simularSensor() {
        String json = "{\"dispositivoId\": \"PULSEIRA-01\", \"tipo\": \"STATUS\", \"valor\": 1.0}";
        publicar("monsai/sensores", json);
    }
}
