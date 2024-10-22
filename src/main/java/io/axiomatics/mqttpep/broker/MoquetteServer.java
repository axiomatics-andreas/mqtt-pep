package io.axiomatics.mqttpep.broker;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class MoquetteServer {

    public static void main(String[] args) throws Exception {
        // Initialize Moquette MQTT Broker
        Server mqttBroker = new Server();
        Properties configProps = new Properties();
        configProps.setProperty("port", "1884");
        configProps.setProperty("host", "0.0.0.0");
        configProps.setProperty("allow_anonymous", "true");
        configProps.setProperty("persistent_qeue_type", "segmented");
        String dataDir = "build/moquette-data-" + System.currentTimeMillis();
        new File(dataDir).mkdirs();
        configProps.setProperty("data_path", dataDir);
        MemoryConfig config = new MemoryConfig(configProps);

        // Inject your custom Authorization Handler
        IAuthorizatorPolicy authorizationPolicy = new MoquettePepXacmlAuthorization(config);


        final List<? extends InterceptHandler> userHandlers = Arrays.asList(new PublisherListener());

        Runnable serverTask = () -> {
            try {
                mqttBroker.startServer(config,  userHandlers, null, null, authorizationPolicy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };  // Inject authorization policy here

        new Thread(serverTask).run();;
        System.out.println("Server started");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Stopping broker");
                 mqttBroker.stopServer();

            }
        });

        System.out.println("Moquette MQTT Broker started with custom authorization policy");

        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            mqttBroker.stopServer();
            System.out.println("Moquette MQTT Broker stopped");
        }));
    }

    static class PublisherListener extends AbstractInterceptHandler {
        @Override
        public void onPublish(InterceptPublishMessage message) {
            System.out.println("moquette mqtt broker message intercepted, topic: " + message.getTopicName() + ", content: " + new String(message.getPayload().array()));
        }

        @Override
        public String getID() {
            return "Serve-1 ID";
        }

        @Override
        public void onSessionLoopError(Throwable error) {

            throw new UnsupportedOperationException("Unimplemented method 'onSessionLoopError'");
        }
    }
}

