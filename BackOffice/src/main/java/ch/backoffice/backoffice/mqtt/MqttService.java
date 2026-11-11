package ch.backoffice.backoffice.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MqttService {

    private static MqttClient client;

    public static void start() {
        try {
            client = new MqttClient("tcp://mqtt.blue.section-inf.ch:1883", MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("ict");
            options.setPassword("321".toCharArray());
            client.connect(options);
            client.subscribe("umddcmid3b/time");
            client.setCallback(new MqttCallback() {
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    System.out.println("Reçu sur " + topic + " : " + payload);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Connexion perdue : " + cause.getMessage());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            client.connect();
            client.subscribe("umddcmid3b/time");
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public static void publish(String topic, String payload) {
        try {
            MqttMessage msg = new MqttMessage(payload.getBytes());
            client.publish(topic, msg);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}