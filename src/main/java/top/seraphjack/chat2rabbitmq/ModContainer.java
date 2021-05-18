package top.seraphjack.chat2rabbitmq;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(ModContainer.MODID)
public final class ModContainer {
    public static final String MODID = "chat2rabbitmq";
    public static final Logger logger = LogManager.getLogger(ModContainer.MODID);

    public ModContainer() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY, (serverVer, isDedicated) -> true));

        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStop);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    void serverStart(FMLServerStartedEvent event) {
        try {
            MQManager.init(Config.uri.get(), Config.postExchange.get());
        } catch (Exception e) {
            logger.error("Failed to initialize MQ manager", e);
        }
    }

    void serverStop(FMLServerStoppingEvent event) {
        try {
            MQManager.instance().stop();
        } catch (Exception e) {
            logger.error("Failed to stop mq manager", e);
        }
    }
}
