package com.github.cao.awa.modmdo.event;

import com.github.cao.awa.modmdo.event.block.destroy.*;
import com.github.cao.awa.modmdo.event.block.place.*;
import com.github.cao.awa.modmdo.event.block.state.*;
import com.github.cao.awa.modmdo.event.entity.damage.*;
import com.github.cao.awa.modmdo.event.entity.death.*;
import com.github.cao.awa.modmdo.event.entity.player.*;
import com.github.cao.awa.modmdo.event.entity.player.chat.*;
import com.github.cao.awa.modmdo.event.server.*;
import com.github.cao.awa.modmdo.event.server.tick.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.operational.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.explosion.*;

public class ModMdoEventTracer {
    public final EntityDeathEvent entityDeath = EntityDeathEvent.snap();
    public final BlockDestroyEvent blockDestroy = BlockDestroyEvent.snap();
    public final BlockPlaceEvent blockPlace = BlockPlaceEvent.snap();
    public final BlockStateSetEvent blockStateSet = BlockStateSetEvent.snap();
    public final BlockExplosionDestroyEvent blockExplosion = BlockExplosionDestroyEvent.snap();
    public final EntityDamageEvent entityDamage = EntityDamageEvent.snap();
    public final JoinServerEvent joinServer = JoinServerEvent.snap();
    public final QuitServerEvent quitServer = QuitServerEvent.snap();
    public final GameTickStartEvent gameTickStart = GameTickStartEvent.snap();
    public final ServerStartedEvent serverStarted = ServerStartedEvent.snap();
    public final GameChatEvent gameChat = GameChatEvent.snap();
    public final Object2ObjectOpenHashMap<String, ModMdoEvent<?>> events = EntrustParser.operation(new Object2ObjectOpenHashMap<>(), map -> {
        map.put(entityDeath.clazz(), entityDeath);
        map.put(blockDestroy.clazz(), blockDestroy);
        map.put(blockPlace.clazz(), blockPlace);
        map.put(blockStateSet.clazz(), blockStateSet);
        map.put(blockExplosion.clazz(), blockExplosion);
        map.put(entityDamage.clazz(), entityDamage);
        map.put(joinServer.clazz(), joinServer);
        map.put(quitServer.clazz(), quitServer);
        map.put(gameTickStart.clazz(), gameTickStart);
        map.put(serverStarted.clazz(), serverStarted);
        map.put(gameChat.clazz(), gameChat);
    });

    public void build() {
        ModMdoEventCenter.callingBuilding.forEach((id, extra) -> extra.initEvent());
    }

    public int registered() {
        OperationalInteger result = new OperationalInteger(0);
        events.forEach((name, event) -> result.add(event.registered()));
        return result.get();
    }

    public void submitBlockDestroy(PlayerEntity player, BlockState state, BlockPos pos, World world, MinecraftServer server) {
        if (blockDestroy.isSubmitted()) {
            blockDestroy.action();
        } else {
            blockDestroy.submit(new BlockDestroyEvent(player, state, pos, world, server));
        }
    }

    public void submitBlockPlace(LivingEntity player, BlockState state, BlockPos pos, World world, ItemStack itemStack, MinecraftServer server) {
        if (blockPlace.isSubmitted()) {
            blockPlace.action();
        } else {
            blockPlace.submit(new BlockPlaceEvent(player, state, pos, world, itemStack, server));
        }
    }

    public void submitBlockExplosion(Explosion explosion, BlockState state, BlockPos pos, World world, MinecraftServer server) {
        blockExplosion.immediately(new BlockExplosionDestroyEvent(explosion, state, pos, world, server));
    }

    public void submitBlockStateSet(BlockState state, BlockPos pos, int flags, int maxUpdateDepth, World world, MinecraftServer server) {
        blockStateSet.immediately(new BlockStateSetEvent(state, pos, flags, maxUpdateDepth, world, server));
    }

    public void submitEntityDeath(LivingEntity entity, LivingEntity perpetrator, Vec3d pos, MinecraftServer server) {
        entityDeath.immediately(new EntityDeathEvent(entity, perpetrator, pos, server));
    }

    public void submitEntityDamage(LivingEntity entity, DamageSource damageSource, float originalHealth, float damage, World world, MinecraftServer server) {
        entityDamage.immediately(new EntityDamageEvent(entity, damageSource, originalHealth, damage, world, server));
    }

    public void submitJoinServer(ServerPlayerEntity player, ClientConnection connection, Vec3d pos, MinecraftServer server) {
        joinServer.immediately(new JoinServerEvent(player, connection, pos, server));
    }

    public void submitQuitServer(ServerPlayerEntity player, ClientConnection connection, Vec3d pos, MinecraftServer server) {
        quitServer.immediately(new QuitServerEvent(player, connection, pos, server));
    }

    public void submitGameTickStart(MinecraftServer server) {
        gameTickStart.skipDelay(new GameTickStartEvent(server));
    }

    public void submitServerStarted(MinecraftServer server) {
        serverStarted.immediately(new ServerStartedEvent(server));
    }

    public void submitGameChat(ServerPlayerEntity player, ChatMessageC2SPacket message, MinecraftServer server) {
        gameChat.immediately(new GameChatEvent(player, message, server));
    }
}