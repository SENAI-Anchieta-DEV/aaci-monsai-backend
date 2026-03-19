package com.senai.monsai.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.ui_interface.controller.TelemetriaController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setAutomaticReconnect(true);
        // define o timeout para o erro de conexão aparecer mais rápido nos testes
        options.setConnectionTimeout(10);
        factory.setConnectionOptions(options);
        return factory;
    }

    // --- SAÍDA ---
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler outbound() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler("monsai-backend-out", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic("monsai/comandos"); // Tópico padrão caso esqueça de passar
        return handler;
    }

    // --- ENTRADA ---
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("monsai-sub", mqttClientFactory(), "monsai/telemetria", "monsai/alertas");
        adapter.setOutputChannel(mqttInputChannel());
        adapter.setErrorChannelName("mqttErrorChannel"); // Vincula o erro ao canal abaixo
        return adapter;
    }

    // --- TRATAMENTO DE ERRO PADRONIZADO (AACI-121) ---
    @Bean
    public MessageChannel mqttErrorChannel() {
        return new DirectChannel();
    }

    @ServiceActivator(inputChannel = "mqttErrorChannel")
    public void handleMqttError(Message<?> message) {
        // Log limpo e profissional
        System.err.println("[MONSAI - MQTT ERRO]: Falha na comunicação com o Broker.");
        System.err.println("Causa/Payload: " + message.getPayload());
    }

    /* @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String payload = message.getPayload().toString();
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();

        System.out.println("[MONSAI - MQTT]: Dado recebido no tópico " + topic);

        try {
            // Converte o texto (String) para o seu objeto (TelemetriaDTO)
            ObjectMapper mapper = new ObjectMapper();
            TelemetriaDTO dto = mapper.readValue(payload, TelemetriaDTO.class);

            // Atualiza o Controller com o OBJETO, para que o React consiga ler!
            TelemetriaController.atualizarDados(dto);

        } catch (Exception e) {
            System.err.println("Erro ao converter JSON do MQTT: " + e.getMessage());
        }
    } */
}
