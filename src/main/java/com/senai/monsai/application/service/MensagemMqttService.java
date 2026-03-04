package com.senai.monsai.application.service;

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

    // Este método é o "gatilho" que dispara quando chega mensagem no MQTT
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void processarRecepcao(Message<String> message /*mensagem vinda do MQTT integration*/) {
        // 1. Extraímos os dados da mensagem
        String topico = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String conteudo = message.getPayload();

        // 2. Criamos o objeto da nossa Entidade
        MensagemMqtt novaMensagem = MensagemMqtt.builder()
                .topico(topico)
                .conteudo(conteudo)
                .build();

        // 3. SALVAMOS NO BANCO DE DADOS
        repository.save(novaMensagem);

        System.out.println("Sucesso! Mensagem salva: " + conteudo);

    }

    @Autowired
    private MqttGateway mqttGateway; // Aquela interface que criamos na MqttConfig

    public void publicar(String topico, String conteudo) {
        mqttGateway.sendToMqtt(conteudo, topico);
        System.out.println("Enviando para o Broker -> Tópico: " + topico + " | Mensagem: " + conteudo);
    }
}
