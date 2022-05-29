package com.github.cao.awa.modmdo.network.forwarder.builder;

import net.minecraft.network.*;

public class ModMdoPacketBuilder {
    private final NetworkSide side;
    private final PacketBuilder<?> builder;

    public ModMdoPacketBuilder(NetworkSide side) {
        this.side = side;
        this.builder = side == NetworkSide.CLIENTBOUND ? new ModMdoClientPacketBuilder() : new ModMdoServerPacketBuilder();
    }

    public PacketBuilder<?> getBuilder() {
        return builder;
    }

    public NetworkSide getSide() {
        return side;
    }
}