package top.seraphjack.chat2rabbitmq;

import com.google.gson.Gson;
import top.seraphjack.chat2rabbitmq.message.ChatMessage;

import java.nio.charset.StandardCharsets;

public final class Utils {
    private static final Gson gson = new Gson();

    public static String serializeChatMessage(ChatMessage msg) {
        return gson.toJson(msg);
    }

    public static ChatMessage deserializeChatMessage(byte[] data) {
        return gson.fromJson(new String(data, StandardCharsets.UTF_8), ChatMessage.class);
    }
}
