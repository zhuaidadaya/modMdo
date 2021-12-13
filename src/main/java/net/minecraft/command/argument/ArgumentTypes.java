package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.command.argument.EntityArgumentType.Serializer;
import net.minecraft.command.argument.NumberRangeArgumentType.FloatRangeArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType.IntRangeArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ArgumentTypes {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Class<?>, ArgumentTypes.Entry<?>> CLASS_MAP = Maps.newHashMap();
    private static final Map<Identifier, ArgumentTypes.Entry<?>> ID_MAP = Maps.newHashMap();

    public ArgumentTypes() {
    }

    public static <T extends ArgumentType<?>> void register(String id, Class<T> argClass, ArgumentSerializer<T> serializer) {
        Identifier identifier = new Identifier(id);
        ArgumentTypes.Entry<T> entry = new ArgumentTypes.Entry(argClass, serializer, identifier);
        CLASS_MAP.put(argClass, entry);
        ID_MAP.put(identifier, entry);
    }

    public static void register() {
        BrigadierArgumentTypes.register();
        register("entity", EntityArgumentType.class, new Serializer());
        register("game_profile", GameProfileArgumentType.class, new ConstantArgumentSerializer(GameProfileArgumentType :: gameProfile));
        register("block_pos", BlockPosArgumentType.class, new ConstantArgumentSerializer(BlockPosArgumentType :: blockPos));
        register("column_pos", ColumnPosArgumentType.class, new ConstantArgumentSerializer(ColumnPosArgumentType :: columnPos));
        register("vec3", Vec3ArgumentType.class, new ConstantArgumentSerializer(Vec3ArgumentType :: vec3));
        register("vec2", Vec2ArgumentType.class, new ConstantArgumentSerializer(Vec2ArgumentType :: vec2));
        register("block_state", BlockStateArgumentType.class, new ConstantArgumentSerializer(BlockStateArgumentType :: blockState));
        register("block_predicate", BlockPredicateArgumentType.class, new ConstantArgumentSerializer(BlockPredicateArgumentType :: blockPredicate));
        register("item_stack", ItemStackArgumentType.class, new ConstantArgumentSerializer(ItemStackArgumentType :: itemStack));
        register("item_predicate", ItemPredicateArgumentType.class, new ConstantArgumentSerializer(ItemPredicateArgumentType :: itemPredicate));
        register("color", ColorArgumentType.class, new ConstantArgumentSerializer(ColorArgumentType :: color));
        register("component", TextArgumentType.class, new ConstantArgumentSerializer(TextArgumentType :: text));
        register("message", MessageArgumentType.class, new ConstantArgumentSerializer(MessageArgumentType :: message));
        register("nbt_compound_tag", NbtCompoundArgumentType.class, new ConstantArgumentSerializer(NbtCompoundArgumentType :: nbtCompound));
        register("nbt_tag", NbtElementArgumentType.class, new ConstantArgumentSerializer(NbtElementArgumentType :: nbtElement));
        register("nbt_path", NbtPathArgumentType.class, new ConstantArgumentSerializer(NbtPathArgumentType :: nbtPath));
        register("objective", ScoreboardObjectiveArgumentType.class, new ConstantArgumentSerializer(ScoreboardObjectiveArgumentType :: scoreboardObjective));
        register("objective_criteria", ScoreboardCriterionArgumentType.class, new ConstantArgumentSerializer(ScoreboardCriterionArgumentType :: scoreboardCriterion));
        register("operation", OperationArgumentType.class, new ConstantArgumentSerializer(OperationArgumentType :: operation));
        register("particle", ParticleEffectArgumentType.class, new ConstantArgumentSerializer(ParticleEffectArgumentType :: particleEffect));
        register("angle", AngleArgumentType.class, new ConstantArgumentSerializer(AngleArgumentType :: angle));
        register("rotation", RotationArgumentType.class, new ConstantArgumentSerializer(RotationArgumentType :: rotation));
        register("scoreboard_slot", ScoreboardSlotArgumentType.class, new ConstantArgumentSerializer(ScoreboardSlotArgumentType :: scoreboardSlot));
        register("score_holder", ScoreHolderArgumentType.class, new net.minecraft.command.argument.ScoreHolderArgumentType.Serializer());
        register("swizzle", SwizzleArgumentType.class, new ConstantArgumentSerializer(SwizzleArgumentType :: swizzle));
        register("team", TeamArgumentType.class, new ConstantArgumentSerializer(TeamArgumentType :: team));
        register("item_slot", ItemSlotArgumentType.class, new ConstantArgumentSerializer(ItemSlotArgumentType :: itemSlot));
        register("resource_location", IdentifierArgumentType.class, new ConstantArgumentSerializer(IdentifierArgumentType :: identifier));
        register("mob_effect", StatusEffectArgumentType.class, new ConstantArgumentSerializer(StatusEffectArgumentType :: statusEffect));
        register("function", CommandFunctionArgumentType.class, new ConstantArgumentSerializer(CommandFunctionArgumentType :: commandFunction));
        register("entity_anchor", EntityAnchorArgumentType.class, new ConstantArgumentSerializer(EntityAnchorArgumentType :: entityAnchor));
        register("int_range", IntRangeArgumentType.class, new ConstantArgumentSerializer(NumberRangeArgumentType :: intRange));
        register("float_range", FloatRangeArgumentType.class, new ConstantArgumentSerializer(NumberRangeArgumentType :: floatRange));
        register("item_enchantment", EnchantmentArgumentType.class, new ConstantArgumentSerializer(EnchantmentArgumentType :: enchantment));
        register("entity_summon", EntitySummonArgumentType.class, new ConstantArgumentSerializer(EntitySummonArgumentType :: entitySummon));
        register("dimension", DimensionArgumentType.class, new ConstantArgumentSerializer(DimensionArgumentType :: dimension));
        register("time", TimeArgumentType.class, new ConstantArgumentSerializer(TimeArgumentType :: time));
        register("uuid", UuidArgumentType.class, new ConstantArgumentSerializer(UuidArgumentType :: uuid));
        if(SharedConstants.isDevelopment) {
            register("test_argument", TestFunctionArgumentType.class, new ConstantArgumentSerializer(TestFunctionArgumentType :: testFunction));
            register("test_class", TestClassArgumentType.class, new ConstantArgumentSerializer(TestClassArgumentType :: testClass));
        }

    }

    @Nullable
    private static ArgumentTypes.Entry<?> byId(Identifier id) {
        return ID_MAP.get(id);
    }

    @Nullable
    private static ArgumentTypes.Entry<?> byClass(ArgumentType<?> type) {
        return CLASS_MAP.get(type.getClass());
    }

    public static <T extends ArgumentType<?>> void toPacket(PacketByteBuf buf, T type) {
        ArgumentTypes.Entry<T> entry = (Entry<T>) byClass(type);
        if(entry == null) {
            LOGGER.error("Could not serialize {} ({}) - will not be sent to client!", type, type.getClass());
            buf.writeIdentifier(new Identifier(""));
        } else {
            buf.writeIdentifier(entry.id);
            entry.serializer.toPacket(type, buf);
        }
    }

    @Nullable
    public static ArgumentType<?> fromPacket(PacketByteBuf buf) {
        Identifier identifier = buf.readIdentifier();
        ArgumentTypes.Entry<?> entry = byId(identifier);
        if(entry == null) {
            LOGGER.error("Could not deserialize {}", identifier);
            return null;
        } else {
            return entry.serializer.fromPacket(buf);
        }
    }

    private static <T extends ArgumentType<?>> void toJson(JsonObject json, T type) {
        ArgumentTypes.Entry<T> entry = (Entry<T>) byClass(type);
        if(entry == null) {
            LOGGER.error("Could not serialize argument {} ({})!", type, type.getClass());
            json.addProperty("type", "unknown");
        } else {
            json.addProperty("type", "argument");
            json.addProperty("parser", entry.id.toString());
            JsonObject jsonObject = new JsonObject();
            entry.serializer.toJson(type, jsonObject);
            if(jsonObject.size() > 0) {
                json.add("properties", jsonObject);
            }
        }

    }

    public static <S> JsonObject toJson(CommandDispatcher<S> dispatcher, CommandNode<S> commandNode) {
        JsonObject jsonObject = new JsonObject();
        if(commandNode instanceof RootCommandNode) {
            jsonObject.addProperty("type", "root");
        } else if(commandNode instanceof LiteralCommandNode) {
            jsonObject.addProperty("type", "literal");
        } else if(commandNode instanceof ArgumentCommandNode) {
            toJson(jsonObject, ((ArgumentCommandNode) commandNode).getType());
        } else {
            LOGGER.error("Could not serialize node {} ({})!", commandNode, commandNode.getClass());
            jsonObject.addProperty("type", "unknown");
        }

        JsonObject jsonObject2 = new JsonObject();

        for(CommandNode<S> sCommandNode : commandNode.getChildren()) {
            jsonObject2.add(sCommandNode.getName(), toJson(dispatcher, sCommandNode));
        }

        if(jsonObject2.size() > 0) {
            jsonObject.add("children", jsonObject2);
        }

        if(commandNode.getCommand() != null) {
            jsonObject.addProperty("executable", true);
        }

        if(commandNode.getRedirect() != null) {
            Collection<String> collection = dispatcher.getPath(commandNode.getRedirect());
            if(! collection.isEmpty()) {
                JsonArray commandNode2 = new JsonArray();

                for(String string : collection) {
                    commandNode2.add(string);
                }

                jsonObject.add("redirect", commandNode2);
            }
        }

        return jsonObject;
    }

    public static boolean hasClass(ArgumentType<?> type) {
        return byClass(type) != null;
    }

    public static <T> Set<ArgumentType<?>> getAllArgumentTypes(CommandNode<T> node) {
        Set<CommandNode<T>> set = Sets.newIdentityHashSet();
        Set<ArgumentType<?>> set2 = Sets.newHashSet();
        getAllArgumentTypes(node, set2, set);
        return set2;
    }

    private static <T> void getAllArgumentTypes(CommandNode<T> node, Set<ArgumentType<?>> argumentTypes, Set<CommandNode<T>> ignoredNodes) {
        if(ignoredNodes.add(node)) {
            if(node instanceof ArgumentCommandNode) {
                argumentTypes.add(((ArgumentCommandNode) node).getType());
            }

            node.getChildren().forEach((nodex) -> {
                getAllArgumentTypes(nodex, argumentTypes, ignoredNodes);
            });
            CommandNode<T> commandNode = node.getRedirect();
            if(commandNode != null) {
                getAllArgumentTypes(commandNode, argumentTypes, ignoredNodes);
            }

        }
    }

    private static class Entry<T extends ArgumentType<?>> {
        public final Class<T> argClass;
        public final ArgumentSerializer<T> serializer;
        public final Identifier id;

        Entry(Class<T> argClass, ArgumentSerializer<T> serializer, Identifier id) {
            this.argClass = argClass;
            this.serializer = serializer;
            this.id = id;
        }
    }
}