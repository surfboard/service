package org.surfing.service.mqtt;

import org.surfing.service.things.Manager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import static org.surfing.kernel.Kernel.APP_NAME;
import org.surfing.Service;

/**
 *
 * @author vsenger
 */
public abstract class MQTTBaseService extends Service {

    public String MQTT_SERVER = "tcp://iot.eclipse.org:1883";
    public String MQTT_QUEUE_SUBSCRIBE = null;
    public String MQTT_QUEUE_PUBLISH = null;

    @Override
    public void start() {
        if (getConfig().getProperty("mqtt.server") != null) {
            MQTT_SERVER = getConfig().getProperty("mqtt.server");
        }
        if (getConfig().getProperty("mqtt.queue.subscribe") != null) {
            MQTT_QUEUE_SUBSCRIBE = getConfig().getProperty("mqtt.queue.subscribe");
        }
        if (getConfig().getProperty("mqtt.queue.publish") != null) {
            MQTT_QUEUE_PUBLISH = getConfig().getProperty("mqtt.queue.publish");
        }

    }

    MqttClient client;

    public void sendMessage(String msg, String queue) throws MqttException {
        try {
            if (client == null) {
                fixConnection();
            }
            if (!client.isConnected()) {
                fixConnection();
            }
            Logger.getLogger(MQTTBaseService.class.getName()).log(Level.INFO, "Sending Message MQTT {0} to Queue {1} on server {2}", new Object[]{msg, queue, client.getServerURI()});

            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            client.publish(queue, message);
            Logger.getLogger(MQTTBaseService.class.getName()).log(Level.INFO, "Message sent!");

        } catch (MqttException ex) {
            Logger.getLogger(MQTTBaseService.class.getName()).log(Level.SEVERE, "Error sending MQTT message " + ex.getMessage());
            try {
                fixConnection();
            } catch (MqttException e) {
                Logger.getLogger(MQTTBaseService.class.getName()).log(Level.SEVERE, "Error fixing connecting " + e.getMessage());
                throw new MqttException(e);
            }
        }

    }

    public void fixConnection() throws MqttException {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException ex) {
        }
        String clientName = APP_NAME + "-" + ((int) (Math.random() * 3000) + 1);
        Logger.getLogger(MQTTBaseService.class.getName()).log(Level.INFO, "Stablishing new MQTT Connection " + MQTT_SERVER);

        client = new MqttClient(MQTT_SERVER, clientName);
        client.connect();
        Logger.getLogger(MQTTBaseService.class.getName()).log(Level.INFO, "Connection Stablished!");

    }

    public void close() throws MqttException {
        client.disconnect();
    }

    @Override
    public void stop() {
        try {
            close();
        } catch (MqttException ex) {
            Logger.getLogger(MQTTBaseService.class.getName()).log(Level.SEVERE, "Error closing MQTT Connection!", ex);
        }
    }

    public void sendFileTesting(File file, String queue) throws MqttException, FileNotFoundException, IOException {
        MqttClient client1 = new MqttClient(MQTT_SERVER, APP_NAME + "-" + System.currentTimeMillis());
        client1.connect();
        //client1.setCallback(new Manager());
        MqttMessage message = new MqttMessage();
        FileInputStream f = new FileInputStream(file);
        byte pic[] = new byte[(int) file.length()];
        f.read(pic);
        message.setPayload(pic);
        client1.publish(queue, message);
        client1.setCallback(null);
        client1.disconnect();
        client1.close();
    }

}
