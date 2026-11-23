package ch.backoffice.backoffice.mqtt;

import ch.backoffice.backoffice.serialisation.TimeJsonDeserializer;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Instant;
import java.util.function.Consumer;

import static java.awt.SystemColor.text;

public class MqttService {

    private static MqttClient client;

    private static Consumer<Integer> timeListener;

    public static void setTimeListener(Consumer<Integer> listener) {
        timeListener = listener;
    }

    public static void start() {
        try {
            client = new MqttClient("tcp://mqtt.blue.section-inf.ch:1883",
                    MqttClient.generateClientId(),
                    new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("ict");
            options.setPassword("321".toCharArray());
            client.setCallback(new MqttCallback() {
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Integer timestamp = TimeJsonDeserializer.deserialize(
                            new String(message.getPayload(), StandardCharsets.UTF_8));
                    if (timestamp == null) return;
                    //System.out.println("Reçu sur " + topic + " : " + timestamp);

                    if (timeListener != null) {
                        timeListener.accept(timestamp);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Connexion perdue : " + cause.getMessage());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            client.connect(options);
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