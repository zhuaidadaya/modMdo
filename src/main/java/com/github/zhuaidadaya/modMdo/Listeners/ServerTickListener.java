package com.github.zhuaidadaya.modMdo.Listeners;

import com.github.zhuaidadaya.modMdo.Commands.DimensionTips;
import com.github.zhuaidadaya.modMdo.Commands.XYZ;
import com.github.zhuaidadaya.modMdo.Lang.Language;
import com.github.zhuaidadaya.modMdo.Storage.Variables;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.zhuaidadaya.modMdo.Storage.Variables.*;

public class ServerTickListener {
    public void serverTickL() {
        AtomicInteger tick = new AtomicInteger(20);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                PlayerManager players = Variables.server.getPlayerManager();

                for(ServerPlayerEntity player : players.getPlayerList()) {
                    if(isUserDeadMessageReceive(player.getUuid())) {
                        if(player.deathTime == 1) {
                            DimensionTips dimensionTips = new DimensionTips();
                            XYZ xyz = new XYZ(player.getX(), player.getY(), player.getZ());
                            player.sendMessage(Text.of(formatDeathMessage(player, dimensionTips, xyz)), false);
                        }
                    }
                }
            } catch (Exception e) {

            }

        });
    }

    public String formatDeathMessage(ServerPlayerEntity player, DimensionTips dimensionTips, XYZ xyz) {
        String dimension = dimensionTips.getDimension(player);
        Language language = getUserLanguage(player.getUuid());
        return String.format(languageDictionary.getWord(language, "dead.deadIn"), dimensionTips.getDimensionColor(dimension), dimensionTips.getDimensionName(language,dimension), xyz.getIntegerXYZ());
    }
}
