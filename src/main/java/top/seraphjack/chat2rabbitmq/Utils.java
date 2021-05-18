package top.seraphjack.chat2rabbitmq;

import com.google.gson.Gson;

public final class Utils {
    private static final Gson gson = new Gson();

    public static String serializeChatMessage(ChatMessage msg) {
        return gson.toJson(msg);
    }
}
