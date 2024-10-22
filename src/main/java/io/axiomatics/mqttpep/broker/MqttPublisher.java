package io.axiomatics.mqttpep.broker;

import org.eclipse.paho.client.mqttv3.*;

public class MqttPublisher {

    private static final String MQTT_BROKER = "tcp://127.0.0.1:1884";
    private static final String MQTT_TOPIC = "test/topic";
    private static final String CLIENT_ID = "publisherClient";

    public static void main(String[] args) {
        try {
            // Initialize MQTT client
            MqttClient mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);

            // Create a message
            String payload = "Hello from publisher!";
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // You can set QoS as 0, 1, or 2
            message.setRetained(false);

            // Publish the message to the topic
            mqttClient.publish(MQTT_TOPIC, message);
            System.out.println("Message published to topic: " + MQTT_TOPIC);

            // Disconnect the client
            mqttClient.disconnect();
            System.out.println("Disconnected from broker");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

