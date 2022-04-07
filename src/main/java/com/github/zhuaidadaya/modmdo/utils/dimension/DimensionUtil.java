package com.github.zhuaidadaya.modmdo.utils.dimension;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class DimensionUtil {
    public String getDimensionColor(String dimension) {
        String result;
        switch(dimension) {
            case "overworld" -> result = "§a";
            case "the_nether" -> result = "§c";
            case "the_end" -> result = "§f";
            default -> result = "";
        }
        return result;
    }

    public String getDimensionKey(String dimension) {
        return "dimension." + dimension;
    }

    public TranslatableText getDimensionName(String dimension) {
        return new TranslatableText(getDimensionKey(dimension));
    }

    public String getDimension(ServerPlayerEntity player) {
        return player.getEntityWorld().getDimension().getEffects().getPath();
    }
}
