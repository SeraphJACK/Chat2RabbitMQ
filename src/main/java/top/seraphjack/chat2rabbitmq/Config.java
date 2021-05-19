package top.seraphjack.chat2rabbitmq;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    public static final ForgeConfigSpec.ConfigValue<String> uri;
    public static final ForgeConfigSpec.ConfigValue<String> postExchange;
    public static final ForgeConfigSpec.ConfigValue<String> receiveExchange;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        uri = builder.define("uri", "amqp://guest:guest@localhost:5672/%2f");
        postExchange = builder.define("postExchange", "minecraft.chat");
        receiveExchange = builder.define("receiveExchange", "minecraft.send");
        SPEC = builder.build();
    }
}
