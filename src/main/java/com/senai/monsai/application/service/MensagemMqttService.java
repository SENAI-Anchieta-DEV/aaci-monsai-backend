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
            // 1. Tentativa de validação do JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            MensagemMqttDTO dto = mapper.readValue(payload, MensagemMqttDTO.class);

            // 2. JSON válido - cria a entidade a partir do DTO
            MensagemMqtt novaMensagem = MensagemMqtt.builder()
                    .topico(topico)
                    .conteudo(dto.conteudo()) // Conteudo validado
                    .dataRecebimento(dto.dataRecebimento()) // salvamento da data vinda do JSON
                    .build();

            repository.save(novaMensagem);
            System.out.println("Mensagem válida salva no banco: " + dto.conteudo());

        } catch (Exception e) {
            // 3. Tratamento de erro padrão
            System.err.println("ERRO: Payload malformado ou contrato violado. Payload: " + payload);
        }
    }

    @Autowired
    private MqttGateway mqttGateway; // Aquela interface que criamos na MqttConfig

    public void publicar(String topico, String conteudo) {
        mqttGateway.sendToMqtt(conteudo, topico);
        System.out.println("Enviando para o Broker -> Tópico: " + topico + " | Mensagem: " + conteudo);
    }
}
