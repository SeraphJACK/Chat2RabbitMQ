package top.seraphjack.chat2rabbitmq.message;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import top.seraphjack.chat2rabbitmq.ModContainer;
import top.seraphjack.chat2rabbitmq.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MQManager {
    private static MQManager INSTANCE;

    public static MQManager instance() {
        if (INSTANCE == null) throw new IllegalStateException();
        return INSTANCE;
    }

    public static void init(String uri, String postExchange, String receiveExchange) throws Exception {
        if (INSTANCE != null) throw new IllegalStateException();
        INSTANCE = new MQManager(uri, postExchange, receiveExchange);
    }

    private final String postExchange, receiveExchange;

    private final Connection connection;
    private Channel postMessageChannel, receiveMessageChannel;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("chat2rabbitmq-worker-%d")
            .build());

    private final AMQP.BasicProperties MESSAGE_PROPERTIES = new AMQP.BasicProperties().builder()
            .contentType("application/json")
            .contentEncoding(StandardCharsets.UTF_8.name())
            .build();

    private MQManager(String uri, String postExchange, String receiveExchange) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(uri);
        connection = factory.newConnection();
        this.postExchange = postExchange;
        this.receiveExchange = receiveExchange;

        initChannels();
    }

    private void initChannels() throws Exception {
        // post channel
        postMessageChannel = connection.createChannel();
        postMessageChannel.exchangeDeclare(postExchange, "fanout", true);

        // receive channel
        receiveMessageChannel = connection.createChannel();
        receiveMessageChannel.exchangeDeclare(receiveExchange, "fanout", true);
        // Anonymous queue for receiving messages
        AMQP.Queue.DeclareOk declareOk = receiveMessageChannel.queueDeclare();
        receiveMessageChannel.queueBind(declareOk.getQueue(), receiveExchange, "");
        receiveMessageChannel.basicConsume(declareOk.getQueue(), this.new Consumer());
    }

    public void postMessage(ChatMessage msg) {
        executor.submit(() -> {
            try {
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
        receiveMessageChannel.close();
        executor.shutdown();
        postMessageChannel.close();
        connection.close();
    }

    class Consumer extends DefaultConsumer {
        public Consumer() {
            super(receiveMessageChannel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            ChatMessage msg;
            try {
                msg = Utils.deserializeChatMessage(body);
            } catch (Exception e) {
                ModContainer.logger.error("Failed to deserialize message", e);
                getChannel().basicReject(envelope.getDeliveryTag(), false);
                return;
            }
            ServerLifecycleHooks.getCurrentServer().deferTask(() -> {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                ITextComponent textComponent = new TranslationTextComponent("chat.type.text",
                        msg.displayName,
                        ForgeHooks.newChatWithLinks(msg.message)
                );
                server.getPlayerList().func_232641_a_(textComponent, ChatType.CHAT, Util.DUMMY_UUID);
            });
            getChannel().basicAck(envelope.getDeliveryTag(), false);

        }
    }
}
