package top.seraphjack.chat2rabbitmq;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    public static ForgeConfigSpec.ConfigValue<String> uri;
    public static ForgeConfigSpec.ConfigValue<String> postExchange;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        uri = builder.define("uri", "amqp://guest:guest@localhost:5672/%2f");
        postExchange = builder.define("postExchange", "minecraft.chat");
        SPEC = builder.build();
    }
}
