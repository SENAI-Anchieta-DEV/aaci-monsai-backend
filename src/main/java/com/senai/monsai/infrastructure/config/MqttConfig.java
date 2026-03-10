package com.senai.monsai.infrastructure.config;

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
        // Opcional: defina timeout para o erro de conexão aparecer mais rápido nos testes
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
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler("monsai-pub", mqttClientFactory());
        handler.setDefaultTopic("monsai/default");
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
                new MqttPahoMessageDrivenChannelAdapter("monsai-sub", mqttClientFactory(), "monsai/sensores", "monsai/alertas");
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
        System.err.println("🚨 [MONSAI - MQTT ERROR]: Falha na comunicação com o Broker.");
        System.err.println("Causa/Payload: " + message.getPayload());
    }
}
