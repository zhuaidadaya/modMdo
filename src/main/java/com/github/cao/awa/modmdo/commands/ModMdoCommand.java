package com.github.cao.awa.modmdo.commands;

import com.github.cao.awa.modmdo.*;
import com.github.cao.awa.modmdo.certificate.*;
import com.github.cao.awa.modmdo.commands.suggester.whitelist.*;
import com.github.cao.awa.modmdo.develop.text.*;
import com.github.cao.awa.modmdo.lang.Language;
import com.github.cao.awa.modmdo.storage.*;
import com.github.cao.awa.modmdo.utils.command.*;
import com.github.cao.awa.modmdo.utils.text.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.receptacle.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.command.argument.*;
import net.minecraft.enchantment.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;

import static com.github.cao.awa.modmdo.storage.SharedVariables.*;
import static net.minecraft.server.command.CommandManager.*;

public class ModMdoCommand extends SimpleCommand {
    public ModMdoCommand register() {
        SharedVariables.commandRegister.register(literal("modmdo").executes(modmdo -> {
            SimpleCommandOperation.sendFeedback(modmdo, formatModMdoDescription(SimpleCommandOperation.getPlayer(modmdo)));
            return 0;
        }).requires(level -> level.hasPermissionLevel(4)).then(literal("here").executes(here -> {
            SimpleCommandOperation.sendFeedback(here, formatConfigReturnMessage("here_command"));
            return 2;
        }).then(literal("enable").executes(enableHere -> {
            SharedVariables.enableHereCommand = true;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(enableHere, formatEnableHere());
            return 1;
        })).then(literal("disable").executes(disableHere -> {
            SharedVariables.enableHereCommand = false;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(disableHere, formatDisableHere());
            return 0;
        }))).then(literal("secureEnchant").executes(secureEnchant -> {
            SimpleCommandOperation.sendFeedback(secureEnchant, formatConfigReturnMessage("secure_enchant"));
            return 2;
        }).then(literal("enable").executes(enableSecureEnchant -> {
            SharedVariables.enableSecureEnchant = true;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(enableSecureEnchant, formatEnableSecureEnchant());
            return 1;
        })).then(literal("disable").executes(disableSecureEnchant -> {
            SharedVariables.enableSecureEnchant = false;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(disableSecureEnchant, formatDisableSecureEnchant());
            return 0;
        }))).then(literal("useModMdoWhitelist").executes(whitelist -> {
            SharedVariables.saveVariables();

            SimpleCommandOperation.sendFeedback(whitelist, formatConfigReturnMessage("modmdo_whitelist"));
            return 2;
        }).then(literal("enable").executes(enableWhitelist -> {
            config.set("modmdo_whitelist", SharedVariables.modmdoWhitelist = true);
            SharedVariables.saveVariables();

            SimpleCommandOperation.sendFeedback(enableWhitelist, formatUseModMdoWhitelist());
            return 1;
        })).then(literal("disable").executes(disableWhitelist -> {
            config.set("modmdo_whitelist", SharedVariables.modmdoWhitelist = false);
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(disableWhitelist, formatDisableModMdoWhitelist());
            return 0;
        }))).then(literal("rejectReconnect").executes(rejectReconnect -> {
            SimpleCommandOperation.sendFeedback(rejectReconnect, formatConfigReturnMessage("reject_reconnect"));
            return 2;
        }).then(literal("enable").executes(reject -> {
            if (SharedVariables.commandApplyToPlayer(1, SimpleCommandOperation.getPlayer(reject), reject)) {
                SharedVariables.enableRejectReconnect = true;
                SharedVariables.saveVariables();

                SimpleCommandOperation.sendFeedback(reject, TextUtil.translatable("reject_reconnect.true.rule.format"));
            }
            return 1;
        })).then(literal("disable").executes(receive -> {
            SharedVariables.enableRejectReconnect = false;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(receive, formatDisableRejectReconnect());
            return 0;
        }))).then(literal("itemDespawnTicks").executes(getDespawnTicks -> {
            SimpleCommandOperation.sendFeedback(getDespawnTicks, formatItemDespawnTicks());
            return 2;
        }).then(literal("become").then(argument("ticks", IntegerArgumentType.integer(- 1)).executes(setTicks -> {
            SharedVariables.itemDespawnAge = IntegerArgumentType.getInteger(setTicks, "ticks");

            SimpleCommandOperation.sendFeedbackAndInform(setTicks, formatItemDespawnTicks());
            return 1;
        }))).then(literal("original").executes(setTicksToDefault -> {
            SharedVariables.itemDespawnAge = 6000;

            SimpleCommandOperation.sendFeedbackAndInform(setTicksToDefault, formatItemDespawnTicks());
            return 2;
        }))).then(literal("timeActive").executes(getTimeActive -> {
            SimpleCommandOperation.sendFeedback(getTimeActive, formatConfigReturnMessage("time_active"));
            return 0;
        }).then(literal("enable").executes(enableTimeActive -> {
            SharedVariables.timeActive = true;

            SharedVariables.saveVariables();

            SimpleCommandOperation.sendFeedback(enableTimeActive, formatConfigReturnMessage("time_active"));
            return 0;
        })).then(literal("disable").executes(disableTimeActive -> {
            SharedVariables.timeActive = false;

            SharedVariables.saveVariables();

            SimpleCommandOperation.sendFeedback(disableTimeActive, formatConfigReturnMessage("time_active"));
            return 0;
        }))).then(literal("loginCheckTimeLimit").executes(getTimeLimit -> {
            SimpleCommandOperation.sendFeedback(getTimeLimit, formatCheckerTimeLimit());
            return 0;
        }).then(argument("ms", IntegerArgumentType.integer(500)).executes(setTimeLimit -> {
            config.set("checker_time_limit", IntegerArgumentType.getInteger(setTimeLimit, "ms"));

            SharedVariables.saveVariables();

            SimpleCommandOperation.sendFeedback(setTimeLimit, formatCheckerTimeLimit());
            return 0;
        }))).then(literal("language").executes(getLanguage -> {
            SimpleCommandOperation.sendFeedback(getLanguage, TextUtil.translatable("language.default", SharedVariables.getLanguage()));
            return 0;
        }).then(literal("zh_cn").executes(chinese -> {
            config.set("default_language", com.github.cao.awa.modmdo.lang.Language.ZH_CN);
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(chinese, TextUtil.translatable("language.default", SharedVariables.getLanguage()));
            return 0;
        })).then(literal("en_us").executes(english -> {
            config.set("default_language", Language.EN_US);
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(english, TextUtil.translatable("language.default", SharedVariables.getLanguage()));
            return 0;
        }))).then(literal("maxEnchantmentLevel").executes(getEnchantControlEnable -> {
            SimpleCommandOperation.sendFeedback(getEnchantControlEnable, TextUtil.translatable(SharedVariables.enchantLevelController.isEnabledControl() ? "enchantment.level.controller.enabled" : "enchantment.level.controller.disabled"));
            return 0;
        }).then(literal("enable").executes(enableEnchantLimit -> {
            SharedVariables.enchantLevelController.setEnabledControl(true);
            SharedVariables.saveEnchantmentMaxLevel();
            SimpleCommandOperation.sendFeedback(enableEnchantLimit, TextUtil.translatable(SharedVariables.enchantLevelController.isEnabledControl() ? "enchantment.level.controller.enabled" : "enchantment.level.controller.disabled"));
            return 0;
        })).then(literal("disable").executes(disableEnchantLimit -> {
            SharedVariables.enchantLevelController.setEnabledControl(false);
            SharedVariables.saveEnchantmentMaxLevel();
            SimpleCommandOperation.sendFeedback(disableEnchantLimit, TextUtil.translatable(SharedVariables.enchantLevelController.isEnabledControl() ? "enchantment.level.controller.enabled" : "enchantment.level.controller.disabled"));
            return 0;
        })).then(literal("limit").then(literal("all").then(argument("all", IntegerArgumentType.integer(0, Short.MAX_VALUE)).executes(setDef -> {
            short level = (short) IntegerArgumentType.getInteger(setDef, "all");
            SharedVariables.enchantLevelController.setAll(level);
            SimpleCommandOperation.sendFeedback(setDef, TextUtil.translatable("enchantment.max.level.limit.all", level));
            return 0;
        })).then(literal("default").executes(recoveryAll -> {
            SharedVariables.enchantLevelController.allDefault();
            SimpleCommandOperation.sendFeedback(recoveryAll, TextUtil.translatable("enchantment.max.level.limit.all.default"));
            return 0;
        }))).then(literal("appoint").then(argument("appoint", EnchantmentArgumentType.enchantment()).executes(getLimit -> {
            Identifier name = EnchantmentHelper.getEnchantmentId(EnchantmentArgumentType.getEnchantment(getLimit, "appoint"));
            short level = SharedVariables.enchantLevelController.get(name).getMax();
            SimpleCommandOperation.sendFeedback(getLimit, TextUtil.translatable("enchantment.max.level.limit", name, level));
            SharedVariables.saveEnchantmentMaxLevel();
            return 0;
        }).then(argument("limit", IntegerArgumentType.integer(0, Short.MAX_VALUE)).executes(setLimit -> {
            Identifier name = EnchantmentHelper.getEnchantmentId(EnchantmentArgumentType.getEnchantment(setLimit, "appoint"));
            short level = (short) IntegerArgumentType.getInteger(setLimit, "limit");
            SharedVariables.enchantLevelController.set(name, level);
            SharedVariables.saveEnchantmentMaxLevel();
            SimpleCommandOperation.sendFeedback(setLimit, TextUtil.translatable("enchantment.max.level.limit", name, level));
            return 0;
        })).then(literal("default").executes(recoveryLevel -> {
            Identifier name = EnchantmentHelper.getEnchantmentId(EnchantmentArgumentType.getEnchantment(recoveryLevel, "appoint"));
            short level = SharedVariables.enchantLevelController.get(name).getDefaultMax();
            SharedVariables.enchantLevelController.set(name, level);
            SharedVariables.saveEnchantmentMaxLevel();
            SimpleCommandOperation.sendFeedback(recoveryLevel, TextUtil.translatable("enchantment.max.level.limit", name, level));
            return 0;
        })))))).then(literal("clearEnchantIfLevelTooHigh").executes(getClear -> {
            SimpleCommandOperation.sendFeedback(getClear, TextUtil.formatRule("enchantment_clear_if_level_too_high", SharedVariables.clearEnchantIfLevelTooHigh ? "enabled" : "disabled"));
            return 0;
        }).then(literal("enable").executes(enableClear -> {
            SharedVariables.clearEnchantIfLevelTooHigh = true;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(enableClear, TextUtil.formatRule("enchantment_clear_if_level_too_high", "enabled"));
            return 0;
        })).then(literal("disable").executes(disableClear -> {
            SharedVariables.clearEnchantIfLevelTooHigh = false;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(disableClear, TextUtil.formatRule("enchantment_clear_if_level_too_high", "disabled"));
            return 0;
        }))).then(literal("rejectNoFallCheat").executes(getRejectNoFall -> {
            SimpleCommandOperation.sendFeedback(getRejectNoFall, TextUtil.translatable(SharedVariables.rejectNoFallCheat ? "player.no.fall.cheat.reject" : "player.no.fall.cheat.receive"));
            return 0;
        }).then(literal("enable").executes(reject -> {
            SharedVariables.rejectNoFallCheat = true;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(reject, TextUtil.translatable(SharedVariables.rejectNoFallCheat ? "player.no.fall.cheat.reject" : "player.no.fall.cheat.receive"));
            return 0;
        })).then(literal("disable").executes(receive -> {
            SharedVariables.rejectNoFallCheat = false;
            SharedVariables.saveVariables();
            SimpleCommandOperation.sendFeedback(receive, TextUtil.translatable(SharedVariables.rejectNoFallCheat ? "player.no.fall.cheat.reject" : "player.no.fall.cheat.receive"));
            return 0;
        }))).then(literal("onlyCheckIdentifier").executes(check -> {
            SimpleCommandOperation.sendFeedback(check, formatConfigReturnMessage("whitelist_only_id"));
            return 0;
        }).then(literal("enable").executes(enable -> {
            config.set("whitelist_only_id", true);
            SimpleCommandOperation.sendFeedback(enable, formatConfigReturnMessage("whitelist_only_id"));
            return 0;
        })).then(literal("disable").executes(disable -> {
            config.set("whitelist_only_id", false);
            SimpleCommandOperation.sendFeedback(disable, formatConfigReturnMessage("whitelist_only_id"));
            return 0;
        }))).then(literal("whitelist").then(literal("remove").then(argument("name", StringArgumentType.string()).suggests(ModMdoWhitelistSuggester::suggestions).executes(remove -> {
                    Certificate wl = ModMdoWhitelistSuggester.getWhiteList(StringArgumentType.getString(remove, "name"));
                    if (SharedVariables.whitelist.containsName(wl.getName())) {
                        SharedVariables.whitelist.remove(wl.getName());
                        SimpleCommandOperation.sendFeedback(remove, TextUtil.translatable("modmdo.whitelist.removed", wl.getName()));
                        SharedVariables.saveVariables();
                        return 0;
                    }
                    SimpleCommandOperation.sendError(remove, TextUtil.translatable("arguments.permanent.whitelist.not.registered"));
                    return - 1;
                }))).then(literal("list").executes(showWhiteList -> {
                    showWhitelist(showWhiteList);
                    return 0;
                }))
                //                        .then(literal("multiple").then(argument("name", ModMdoWhitelistArgumentType.whitelist()).executes(remove -> {
                //            Certificate wl = ModMdoWhitelistArgumentType.getWhiteList(remove, "name");
                //            if (temporaryStation.containsName(wl.getName())) {
                //                SimpleCommandOperation.sendFeedback(remove, TextUtil.translatable("modmdo.whitelist.multiple.already", wl.getName()));
                //                return -1;
                //            }
                //            if (SharedVariables.whitelist.containsName(wl.getName())) {
                //                temporaryStation.put(wl.getName(), new TemporaryCertificate(wl.getName(), new LoginRecorde(null, null,LoginRecordeType.MULTIPLE), 0,0));
                //                SimpleCommandOperation.sendFeedback(remove, TextUtil.translatable("modmdo.whitelist.multiple", wl.getName()));
                //                SharedVariables.updateWhitelistNames(SimpleCommandOperation.getServer(remove), true);
                //                SharedVariables.saveVariables();
                //                return 0;
                //            }
                //            SimpleCommandOperation.sendError(remove, TextUtil.translatable("arguments.permanent.whitelist.not.registered"));
                //            return - 1;
                //        })))
        ).then(literal("compatibleOnlineMode").executes(getCompatible -> {
            SimpleCommandOperation.sendFeedback(getCompatible, formatConfigReturnMessage("compatible_online_mode"));
            return 0;
        }).then(literal("enable").executes(enable -> {
            config.set("compatible_online_mode", true);
            SimpleCommandOperation.sendFeedback(enable, formatConfigReturnMessage("compatible_online_mode"));
            return 0;
        })).then(literal("disable").executes(disable -> {
            config.set("compatible_online_mode", false);
            SimpleCommandOperation.sendFeedback(disable, formatConfigReturnMessage("compatible_online_mode"));
            return 0;
        }))).then(literal("modmdoConnecting").executes(modmdoConnecting -> {
            SimpleCommandOperation.sendFeedback(modmdoConnecting, formatConfigReturnMessage("modmdo_connecting"));
            return 0;
        }).then(literal("enable").executes(enable -> {
            config.set("modmdo_connecting", true);
            SimpleCommandOperation.sendFeedback(enable, formatConfigReturnMessage("modmdo_connecting"));
            return 0;
        })).then(literal("disable").executes(disable -> {
            config.set("modmdo_connecting", false);
            SimpleCommandOperation.sendFeedback(disable, formatConfigReturnMessage("modmdo_connecting"));
            return 0;
        }))).then(literal("event").then(literal("list").executes(list -> {
            StringBuilder builder = new StringBuilder();
            event.events.forEach((k, v) -> {
                if (v.registered() > 0) {
                    ObjectArrayList<String> registered = v.getRegistered();
                    builder.append("§7").append(k).append(": ").append("\n");
                    for (String s : registered) {
                        builder.append(s.startsWith("Mod") ? "§b" : "§6").append(" ").append(s).append("\n");
                    }
                }
            });
            builder.append(minecraftTextFormat.format(loginUsers.getUser(getPlayer(list)), "modmdo.event.total", event.registered()).getString());
            sendFeedback(list, TextUtil.translatable(builder.toString()));
            return 0;
        })).then(literal("reload").executes(e -> {
            Legacy<Integer, Integer> legacy = ModMdoStdInitializer.loadEvent(true);
            sendFeedback(e, TextUtil.translatable("modmdo.event.reload.success", legacy.newly(), legacy.stale()));
            return 0;
        }))));
        return this;
    }

    public void showWhitelist(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        SharedVariables.handleTemporaryWhitelist();
        ServerPlayerEntity player = SimpleCommandOperation.getPlayer(source);
        if (SharedVariables.whitelist.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (Certificate wl : SharedVariables.whitelist.values()) {
                builder.append(wl.getName()).append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            SimpleCommandOperation.sendMessage(player, TextUtil.translatable("commands.modmdo.whitelist.list", SharedVariables.whitelist.size(), builder.toString()), false);
        } else {
            SimpleCommandOperation.sendMessage(player, TextUtil.translatable("commands.modmdo.whitelist.none"), false);

        }
    }

    public Translatable formatConfigCachedReturnMessage(String config) {
        return TextUtil.translatable(config + "." + SharedVariables.config.getConfigString(config) + ".rule.format");
    }

    public Translatable formatConfigReturnMessage(String config) {
        return TextUtil.translatable(config + "." + SharedVariables.config.getConfigString(config) + ".rule.format");
    }

    public Translatable formatCheckerTimeLimit() {
        return TextUtil.translatable("checker_time_limit.rule.format", config.getConfigInt("checker_time_limit"));
    }

    public Translatable formatItemDespawnTicks() {
        if (SharedVariables.itemDespawnAge > - 1) {
            return TextUtil.translatable("item.despawn.ticks.rule.format", SharedVariables.itemDespawnAge);
        } else {
            return TextUtil.translatable("item.despawn.ticks.false.rule.format", SharedVariables.itemDespawnAge);
        }
    }

    public Translatable formatEnableHere() {
        return TextUtil.translatable("here_command.true.rule.format");
    }

    public Translatable formatDisableHere() {
        return TextUtil.translatable("here_command.false.rule.format");
    }

    public Translatable formatEnableSecureEnchant() {
        return TextUtil.translatable("secure_enchant.true.rule.format");
    }

    public Translatable formatDisableSecureEnchant() {
        return TextUtil.translatable("secure_enchant.false.rule.format");
    }

    public Translatable formatUseModMdoWhitelist() {
        return TextUtil.translatable("modmdo_whitelist.true.rule.format");
    }

    public Translatable formatDisableModMdoWhitelist() {
        return TextUtil.translatable("modmdo_whitelist.false.rule.format");
    }

    public Translatable formatDisableRejectReconnect() {
        return TextUtil.translatable("reject_reconnect.reject.false.rule.format");
    }

    public Translatable formatModMdoDescription(ServerPlayerEntity player) {
        Translatable modmdoVersion;
        String name;
        if (SharedVariables.getPlayerModMdoVersion(player) > 0 && (name = getPlayerModMdoName(player)) != null) {
            modmdoVersion = TextUtil.translatable("modmdo.description.your.modmdo", name);
        } else {
            modmdoVersion = TextUtil.translatable("modmdo.description.you.do.not.have.modmdo");
        }
        return TextUtil.translatable("modmdo.description", SharedVariables.MODMDO_VERSION_NAME, SharedVariables.RELEASE_TIME, modmdoVersion);
    }
}
