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

    @Value("${mqtt.client-id}")
    private String clientId;

    // 1. Configuração da Fábrica de Clientes (Conexão)
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { brokerUrl });
        options.setCleanSession(true);
        // Reconexão validada
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        // Caso broker exija senha e usuario
        // options.setUserName("seu_usuario");
        // options.setPassword("sua_senha".toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    // --- FLUXO DE SAÍDA (ENVIAR MENSAGENS) ---

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler outbound() {
        // O clientId de envio deve ser diferente do de recepção para evitar conflitos
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(clientId + "-pub", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("monsai/default");
        return messageHandler;
    }

    // --- FLUXO PARA RECEBER MENSAGENS ---

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        // Define o tópico que o sistema vai "escutar" automaticamente
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "-sub",
                        mqttClientFactory(), "monsai/sensores", "monsai/alertas");

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1); // Garantia de entrega pelo menos uma vez
        adapter.setOutputChannel(mqttInputChannel()); // Joga a mensagem no canal de entrada

        // LOG DE ERRO: Se a conexão cair ou o JSON vier errado
        adapter.setErrorChannelName("mqttErrorChannel");

        return adapter;
    }
}
