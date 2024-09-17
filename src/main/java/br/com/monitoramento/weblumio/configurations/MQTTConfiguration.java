package br.com.monitoramento.weblumio.configurations;

import br.com.monitoramento.weblumio.consumers.MQTTSensorConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MQTTConfiguration implements CommandLineRunner {

    private final MQTTSensorConsumer mqttSensorConsumer;

    @Autowired
    public MQTTConfiguration(MQTTSensorConsumer mqttSensorConsumer) {
        this.mqttSensorConsumer = mqttSensorConsumer;
    }

    @Override
    public void run(String... args) {
        this.mqttSensorConsumer.startListening();
    }

}
