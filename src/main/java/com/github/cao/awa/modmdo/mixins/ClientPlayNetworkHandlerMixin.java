package com.github.cao.awa.modmdo.mixins;

import com.github.cao.awa.modmdo.commands.argument.*;
import com.github.cao.awa.modmdo.storage.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import com.mojang.authlib.*;
import io.netty.buffer.*;
import net.minecraft.client.*;
import net.minecraft.client.network.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;
import net.minecraft.network.listener.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.*;
import org.json.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import static com.github.cao.awa.modmdo.storage.SharedVariables.*;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayPacketListener {
    @Shadow
    @Final
    private ClientConnection connection;

    @Shadow
    @Final
    private GameProfile profile;

    @Shadow @Final private MinecraftClient client;

    /**
     * 与服务端进行自定义通信
     *
     * @param packet
     *         packet
     * @param ci
     *         callback
     * @author 草二号机
     * @author 草
     * @author zhuaidadaya
     */
    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onOnCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (SharedVariables.isActive()) {
            PacketByteBuf data = packet.getData();

            try {
                Identifier channel = packet.getChannel();

                Identifier informationSign = EntrustParser.tryCreate(data::readIdentifier, new Identifier(""));

                LOGGER.info("server sent a payload in channel: " + channel);

                if (informationSign.equals(CHECKING) || informationSign.equals(LOGIN)) {
                    System.out.println(staticConfig.getConfigString("identifier"));
                    connection.send(new CustomPayloadC2SPacket(CLIENT, (new PacketByteBuf(Unpooled.buffer())).writeString(LOGIN.toString()).writeString(profile.getName()).writeString(PlayerEntity.getUuidFromProfile(profile).toString()).writeString(EntrustParser.getNotNull(staticConfig.getConfigString("identifier"), "")).writeString(String.valueOf(MODMDO_VERSION)).writeString(client.getLanguageManager().getLanguage().getName())));
                }

                if (informationSign.equals(DATA)) {
                    String data1 = EntrustParser.tryCreate(data::readString, "");
                    String data2 = EntrustParser.tryCreate(data::readString, "");

                    switch (data1) {
                        case "whitelist_names" -> {
                            whitelist.clear();
                            JSONObject json = new JSONObject(data2);
                            for (Object o : json.getJSONArray("names")) {
                                whitelist.put(o.toString(), null);
                            }
                        }
                        case "temporary_whitelist_names" -> {
                            temporaryWhitelist.clear();
                            JSONObject json = new JSONObject(data2);
                            for (Object o : json.getJSONArray("names")) {
                                temporaryWhitelist.put(o.toString(), null);
                            }
                        }
                        case "connections" -> EntrustExecution.tryTemporary(() -> {
                            modmdoConnectionNames.clear();
                            JSONObject json = new JSONObject(data2);
                            for (Object o : json.getJSONArray("names")) {
                                modmdoConnectionNames.add(o.toString());
                            }
                        });
                        case "ban_names" -> {
                            banned.clear();
                            JSONObject json = new JSONObject(data2);
                            for (Object o : json.getJSONArray("names")) {
                                banned.put(o.toString(), null);
                            }
                        }
                    }
                    ArgumentInit.init();
                }
            } catch (Exception e) {
                LOGGER.error("error in connecting ModMdo server", e);
            }
            ci.cancel();
        }
    }
}