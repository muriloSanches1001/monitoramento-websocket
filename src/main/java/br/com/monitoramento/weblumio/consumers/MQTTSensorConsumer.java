package br.com.monitoramento.weblumio.consumers;

import br.com.monitoramento.weblumio.controllers.SensorDataController;
import br.com.monitoramento.weblumio.entities.sensorData.SensorDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MQTTSensorConsumer {

    private final SensorDataController sensorDataController;

    @Autowired
    public MQTTSensorConsumer(
            SensorDataController sensorDataController
    ) {
        this.sensorDataController = sensorDataController;
    }

    private final String broker = "tcp://134.122.6.232:1883";
    private final String clientId = "JavaMqttClient";
    private final String topic = "/sensor";

    public void startListening() {
        try {
            MqttClient mqttClient = new MqttClient(broker, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("monitoramento");
            options.setPassword("password".toCharArray());
            mqttClient.connect(options);

            mqttClient.subscribe(topic, (topic, msg) -> {
                String payload = new String(msg.getPayload());

                // Processar a mensagem recebida
                log.info("Message in topic '{}', message: {}", topic, payload);

                String[] parts = payload.split(",");
                Long sensorId = Long.parseLong(parts[0]);
                Short sensorValue = Short.parseShort(parts[1]);

                SensorDataDTO sensorDataDTO = new SensorDataDTO(
                      sensorId,
                      sensorValue
                );

                this.sensorDataController.create(sensorDataDTO);
            });

        } catch (MqttException e) {
            log.error("Erro ao conectar ou subscrever ao broker MQTT: {}", e.getMessage(), e);
        }
    }

}
