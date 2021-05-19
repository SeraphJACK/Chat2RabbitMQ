package top.seraphjack.chat2rabbitmq.message;

import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;

public final class ChatMessage {
    UUID uuid;
    String username, displayName, message;
    long time;

    public ChatMessage(ServerPlayerEntity player, String msg) {
        this.uuid = player.getUniqueID();
        this.username = player.getScoreboardName();
        this.displayName = player.getDisplayName().getString();
        this.message = msg;
        this.time = System.currentTimeMillis();
    }
}
