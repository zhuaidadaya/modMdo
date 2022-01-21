package com.github.zhuaidadaya.modMdo.mixins;

import com.github.zhuaidadaya.modMdo.type.ModMdoType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static com.github.zhuaidadaya.modMdo.storage.Variables.*;

/**
 * TAG:DRT|SKP|VSD
 * 这个tag用于注明这是有版本差异的
 * 存在这个tag时不会直接从其他正在开发的部分复制
 * 而是手动替换
 * TAG:
 * DRT(Don't Replace It)
 * SKP(Skip)
 * VSD(Version Difference)
 * <p>
 * 手动替换检测: 1.17.x
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    @Final
    public ClientConnection connection;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    protected abstract boolean isHost();

    /**
     * 解析玩家发送的数据包, 如果identifier为 <code>modmdo:token</code> 则检查token
     * token正确就加入loginUsers中, 加入就算放行了
     *
     * @param packet
     *         客户端发送的数据包
     * @param ci
     *         callback
     *
     * @author 草awa
     * @author 草二号机
     * @author zhuaidadaya
     */
    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        try {
            Identifier channel = new Identifier("");

            try {
                channel = packet.getChannel();
            } catch (Exception e) {

            }

            PacketByteBuf packetByteBuf = null;
            try {
                packetByteBuf = new PacketByteBuf(packet.getData().copy());
            } catch (Exception e) {
            }


            String data1 = "";
            try {
                data1 = packetByteBuf.readString();
            } catch (Exception e) {

            }

            String data2 = "";
            try {
                data2 = packetByteBuf.readString();
            } catch (Exception e) {

            }

            String data3 = "";
            try {
                data3 = packetByteBuf.readString();
            } catch (Exception e) {

            }

            String data4 = "";
            try {
                data4 = packetByteBuf.readString();
            } catch (Exception e) {

            }

            String data5 = "";
            try {
                data5 = packetByteBuf.readString();
            } catch (Exception e) {

            }

            String data6 = "";
            try {
                data6 = packetByteBuf.readString();
            } catch (Exception e) {

            }

            if(enableEncryptionToken & modMdoType == ModMdoType.SERVER) {
                serverLogin.login(channel, data1, data2, data3, data4, data5, data6);
            }

            ci.cancel();
        } catch (Exception e) {
        }
    }

    /**
     * 在玩家退出游戏后去除放行状态, 下一次进入也需要重传token
     *
     * @param reason
     *         移除信息
     *
     * @author 草awa
     * @author 草二号机
     * @author zhuaidadaya
     *
     * @reason
     */
    @Overwrite
    public void onDisconnected(Text reason) {
        new Thread(() -> {
            Thread.currentThread().setName("ModMdo accepting");

            if(loginUsers.hasUser(player)) {
                LOGGER.info("{} lost connection: {}", this.player.getName().getString(), reason.getString());
                this.server.forcePlayerSampleUpdate();
                if(loginUsers.hasUser(player))
                    this.server.getPlayerManager().broadcastChatMessage((new TranslatableText("multiplayer.player.left", this.player.getDisplayName())).formatted(Formatting.YELLOW), MessageType.SYSTEM, Util.NIL_UUID);
                this.player.onDisconnect();
                this.server.getPlayerManager().remove(this.player);
                this.player.getTextStream().onDisconnect();
                if(this.isHost()) {
                    LOGGER.info("Stopping singleplayer server as player logged out");
                    this.server.stop(false);
                }
                if(enableEncryptionToken) {
                    serverLogin.logout(player);
                }
            }
        }).start();
    }

    /**
     * 不登入不给移动
     *
     * @param packet
     *         移动请求
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    public void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }

    /**
     * 不登入不给移动
     *
     * @param packet
     *         移动请求
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "onVehicleMove", at = @At("HEAD"), cancellable = true)
    public void onVehicleMove(VehicleMoveC2SPacket packet, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }

    /**
     * 不登入不给输入(
     *
     * @param packet
     *         输入请求
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "onPlayerInput", at = @At("HEAD"), cancellable = true)
    public void onPlayerInput(PlayerInputC2SPacket packet, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }

    /**
     * 不登入不给传送
     *
     * @param x
     *         x
     * @param y
     *         y
     * @param z
     *         z
     * @param yaw
     *         yaw
     * @param pitch
     *         pitch
     * @param flags
     *         flags
     * @param shouldDismount
     *         should dismount
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "requestTeleport(DDDFFLjava/util/Set;Z)V", at = @At("HEAD"), cancellable = true)
    public void requestTeleport(double x, double y, double z, float yaw, float pitch, Set<PlayerPositionLookS2CPacket.Flag> flags, boolean shouldDismount, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }

    /**
     * 不登入不给使用命令
     *
     * @param input
     *         命令
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "executeCommand", at = @At("HEAD"), cancellable = true)
    private void executeCommand(String input, CallbackInfo ci) {
        LOGGER.info(player.getName().asString() + "(" + player.getUuid().toString() + ") run the command: " + input);
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            LOGGER.info("rejected command request: not login user");
            ci.cancel();
        }
    }

    //    /**
    //     * 不登入不给保持live状态
    //     *
    //     * @author 草二号机
    //     *
    //     * @param packet live包
    //     * @param ci callback
    //     */
    //    @Inject(method = "onKeepAlive", at = @At("HEAD"), cancellable = true)
    //    public void onKeepAlive(KeepAliveC2SPacket packet, CallbackInfo ci) {
    //        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
    //            ci.cancel();
    //        }
    //    }

    /**
     * 草
     *
     * @param packet
     *         packet
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "onSpectatorTeleport", at = @At("HEAD"), cancellable = true)
    public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }

    /**
     * 不登入不给于物品交互
     *
     * @param packet
     *         packet
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "onPlayerInteractItem", at = @At("HEAD"), cancellable = true)
    public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }

    /**
     * 不登入不给于实体交互
     *
     * @param packet
     *         packet
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }

    /**
     * 不登入不给于方块交互
     *
     * @param packet
     *         packet
     * @param ci
     *         callback
     *
     * @author 草二号机
     */
    @Inject(method = "onPlayerInteractBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        if(! loginUsers.hasUser(player) & enableEncryptionToken) {
            ci.cancel();
        }
    }
}