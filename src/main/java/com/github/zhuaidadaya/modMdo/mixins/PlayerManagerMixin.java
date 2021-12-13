package com.github.zhuaidadaya.modMdo.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.zhuaidadaya.modMdo.storage.Variables.enableRejectReconnect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @Shadow
    @Final
    private Map<UUID, ServerPlayerEntity> playerMap;

    @Inject(method = "onPlayerConnect", at = @At("HEAD"), cancellable = true)
    private void onPlayerConnected(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        //        if(server.getPlayerManager().getPlayerList().contains(player)) {
        //            ci.cancel();
        //        }
    }

    @Inject(method = "createPlayer",at = @At("HEAD"))
    public void createPlayer(GameProfile profile, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if(enableRejectReconnect) {
            UUID uUID = PlayerEntity.getUuidFromProfile(profile);
            for(ServerPlayerEntity serverPlayerEntity : this.players) {
                if(serverPlayerEntity.getUuid().equals(uUID))
                    cir.cancel();
            }
        }
    }
}