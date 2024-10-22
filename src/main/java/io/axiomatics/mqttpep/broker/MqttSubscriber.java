package io.axiomatics.mqttpep.broker;

import org.eclipse.paho.client.mqttv3.*;

public class MqttSubscriber {

    private static final String MQTT_BROKER = "tcp://127.0.0.1:1884";
    private static final String MQTT_TOPIC = "test/topic";
    private static final String CLIENT_ID = "subscriberClient";

    public static void main(String[] args) {
        try {
            // Initialize MQTT client
            MqttClient mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Message received from topic " + topic + ": " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not needed for subscriber
                }
            });

            // Subscribe to topic
            System.out.println("We can subscribe");
            mqttClient.subscribe(MQTT_TOPIC);
            System.out.println("Subscribed to topic: " + MQTT_TOPIC);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

