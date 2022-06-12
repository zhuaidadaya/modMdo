package com.github.cao.awa.modmdo.event.client;

import com.github.cao.awa.modmdo.annotations.*;
import com.github.cao.awa.modmdo.event.delay.*;
import com.github.cao.awa.modmdo.event.entity.*;
import com.github.cao.awa.modmdo.utils.entity.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;

@Auto
public class ClientSettingEvent extends EntityTargetedEvent<ClientSettingEvent> {
    private final ServerPlayerEntity player;
    private final ClientSettingsC2SPacket packet;
    private final MinecraftServer server;

    public ClientSettingEvent(ServerPlayerEntity player, ClientSettingsC2SPacket packet, MinecraftServer server) {
        this.player = player;
        this.packet = packet;
        this.server = server;
    }

    private ClientSettingEvent() {
        this.player = null;
        this.packet = null;
        this.server = null;
    }

    public static ClientSettingEvent snap() {
        return new ClientSettingEvent();
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public ObjectArrayList<LivingEntity> getTargeted() {
        ObjectArrayList<LivingEntity> list = new ObjectArrayList<>();
        list.add(player);
        return list;
    }

    public ClientSettingsC2SPacket getPacket() {
        return packet;
    }

    public String getLanguage() {
        return packet.language();
    }

    public MinecraftServer getServer() {
        return server;
    }

    public ClientSettingEvent fuse(Previously<ClientSettingEvent> previously, ClientSettingEvent delay) {
        return previously.target();
    }

    public String synopsis() {
        String name = EntrustParser.trying(() -> EntrustParser.tryCreate(() -> {
            String str = EntityUtil.getName(player);
            if (str.equals("")) {
                throw new IllegalArgumentException("empty name");
            }
            return str;
        }, player.toString()), () -> "null");
        return EntrustParser.tryCreate(() -> String.format("ClientSettingEvent{player=%s, language=%s, view-distance=%s}", name, packet.language(), packet.viewDistance()), toString());
    }

    @Override
    public String abbreviate() {
        return "ClientSettingEvent";
    }

    public String clazz() {
        return getClass().getName();
    }
}