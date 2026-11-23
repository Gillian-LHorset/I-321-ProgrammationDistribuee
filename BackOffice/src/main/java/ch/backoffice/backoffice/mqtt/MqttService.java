package ch.backoffice.backoffice.mqtt;

import ch.backoffice.backoffice.serialisation.TimeJsonDeserializer;

import org.eclipse.paho.client.mqttv3.*;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import java.nio.charset.StandardCharsets;

import java.util.Date;

import java.time.Instant;

import java.util.function.Consumer;


public class MqttService {


    private static MqttClient client;

    private static Consumer<Integer> timeListener;

    /** Dernier instant (ms) où une enveloppe SERV-Time a été reçue. */
    private static volatile long lastServTimeAt = 0L;

    /** Si aucune enveloppe SERV-Time depuis ce délai, on accepte le format plat. */
    private static final long SERV_TIME_TIMEOUT_MS = 5000L;

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
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    // Log du payload JSON brut (diagnostic)
                    System.out.println("MQTT [" + topic + "] payload brut : " + payload);

                    Integer timestamp = TimeJsonDeserializer.deserialize(payload);
                    if (timestamp == null) return;

                    long now = System.currentTimeMillis();
                    if (TimeJsonDeserializer.isServTimeEnvelope(payload)) {
                        // Enveloppe SERV-Time : source faisant autorité (l'offset y est déjà appliqué)
                        lastServTimeAt = now;
                        if (timeListener != null) {
                            timeListener.accept(timestamp);
                        }
                    } else if (now - lastServTimeAt > SERV_TIME_TIMEOUT_MS) {
                        // SERV-Time silencieux : on retombe sur le format plat (instance legacy)
                        if (timeListener != null) {
                            timeListener.accept(timestamp);
                        }
                    }
                    // Sinon : message plat d'une seconde instance publiée en parallèle
                    // sur le même topic -> ignoré (mais visible dans le log brut ci-dessus)
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
