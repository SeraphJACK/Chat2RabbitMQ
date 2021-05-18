package top.seraphjack.chat2rabbitmq;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MQManager {
    private static MQManager INSTANCE;

    public static MQManager instance() {
        if (INSTANCE == null) throw new IllegalStateException();
        return INSTANCE;
    }

    public static void init(String uri, String postExchange) throws Exception {
        if (INSTANCE != null) throw new IllegalStateException();
        INSTANCE = new MQManager(uri, postExchange);
    }

    private final Connection connection;
    private Channel postMessageChannel;
    private final String postExchange;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("chat2rabbitmq-worker-%d")
            .build());

    private final AMQP.BasicProperties MESSAGE_PROPERTIES = new AMQP.BasicProperties().builder()
            .contentType("application/json")
            .contentEncoding(StandardCharsets.UTF_8.name())
            .build();

    private MQManager(String uri, String postExchange) throws Exception {
        this.postExchange = postExchange;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(uri);
        connection = factory.newConnection();

        postMessageChannel = connection.createChannel();
        postMessageChannel.exchangeDeclare(postExchange, "fanout", true);
    }

    public void postMessage(ChatMessage msg) {
        executor.submit(() -> {
            try {
                if (!postMessageChannel.isOpen()) {
                    ModContainer.logger.warn("Post message channel closed, recreating");
                    postMessageChannel = connection.createChannel();
                }
                postMessageChannel.basicPublish(postExchange, "",
                        MESSAGE_PROPERTIES,
                        Utils.serializeChatMessage(msg).getBytes(StandardCharsets.UTF_8)
                );
            } catch (Exception e) {
                ModContainer.logger.error("Failed to post message to mq", e);
            }
        });
    }

    public void stop() throws Exception {
        executor.shutdown();
        postMessageChannel.close();
        connection.close();
    }
}
