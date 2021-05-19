package top.seraphjack.chat2rabbitmq;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.seraphjack.chat2rabbitmq.message.ChatMessage;
import top.seraphjack.chat2rabbitmq.message.MQManager;

@Mod.EventBusSubscriber(modid = ModContainer.MODID, value = Dist.DEDICATED_SERVER)
public final class EventHandler {

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ChatMessage msg = new ChatMessage(event.getPlayer(), event.getMessage());
        MQManager.instance().postMessage(msg);
    }
}
