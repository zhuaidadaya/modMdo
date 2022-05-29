package com.github.cao.awa.modmdo.extra.loader;

import com.github.cao.awa.modmdo.commands.*;
import com.github.cao.awa.modmdo.commands.argument.*;
import com.github.cao.awa.modmdo.event.trigger.*;
import com.github.cao.awa.modmdo.event.variable.*;
import com.github.cao.awa.modmdo.format.console.*;
import com.github.cao.awa.modmdo.format.minecraft.*;
import com.github.cao.awa.modmdo.lang.*;
import com.github.cao.awa.modmdo.reads.*;
import com.github.cao.awa.modmdo.resourceLoader.*;
import com.github.cao.awa.modmdo.storage.*;
import com.github.zhuaidadaya.rikaishinikui.handler.config.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import net.minecraft.server.*;
import org.json.*;

import java.io.*;

import static com.github.cao.awa.modmdo.ModMdoStdInitializer.*;
import static com.github.cao.awa.modmdo.storage.SharedVariables.*;

public class ModMdo extends ModMdoExtra<ModMdo> {
    private MinecraftServer server;

    public void init() {
        String path = getServerLevelPath(getServer()) + "modmdo/configs";
        File file = new File(path + "/compress.txt");
        if (!file.isFile()) {
            EntrustExecution.tryTemporary(file::createNewFile);
        }
        boolean compress = EntrustParser.trying(() -> Boolean.parseBoolean(FileReads.strictRead(new BufferedInputStream(new FileInputStream(path + "/compress.txt")))), () -> false);
        config = new DiskObjectConfigUtil(entrust, path, "modmdo", compress);

        allDefault();
        defaultConfig();

        try {
            initModMdoVariables();
        } catch (Exception e) {

        }

        saveVariables();
    }

    public MinecraftServer getServer() {
        return server;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public void initCommand() {
        try {
            new ModMdoUserCommand().register().init();
            new HereCommand().register();
            new DimensionHereCommand().register();
            new RankingCommand().register();
            new TestCommand().register();
            new TemporaryWhitelistCommand().register();
            new ModMdoCommand().register();

            ArgumentInit.init();
        } catch (Exception e) {

        }
    }

    public void initStaticCommand() {
    }

    public void initEvent() {
        triggerBuilder = new ModMdoTriggerBuilder();

        EntrustExecution.tryTemporary(() -> {
            new File(getServerLevelPath(getServer()) + "/modmdo/resources/events/").mkdirs();

            EntrustExecution.tryFor(EntrustParser.getNotNull(new File(getServerLevelPath(getServer()) + "/modmdo/resources/events/").listFiles(), new File[0]), file -> {
                EntrustExecution.tryTemporary(() -> {
                    if (file.isFile()) {
                        triggerBuilder.register(new JSONObject(FileReads.read(new BufferedReader(new FileReader(file)))), file);
                        LOGGER.info("Registered event: " + file.getPath());
                    }
                }, ex -> {
                    LOGGER.warn("Failed register event: " + file.getPath(), ex);
                });
            });
        }, Throwable::printStackTrace);

        variables.clear();
        variableBuilder = new ModMdoVariableBuilder();
        EntrustExecution.tryTemporary(() -> {
            new File(getServerLevelPath(getServer()) + "/modmdo/resources/persistent/").mkdirs();

            EntrustExecution.tryFor(EntrustParser.getNotNull(new File(getServerLevelPath(getServer()) + "/modmdo/resources/persistent/").listFiles(), new File[0]), file -> {
                EntrustExecution.notNull(variableBuilder.build(file, new JSONObject(FileReads.read(new BufferedReader(new FileReader(file))))), v -> {
                    variables.put(v.getName(), v);
                });
            });
        });

        Resource<Language> resource = new Resource<>();
        resource.set(Language.CHINESE, "/assets/modmdo/lang/zh_cn.json");
        resource.set(Language.ENGLISH, "/assets/modmdo/lang/en_us.json");

        EntrustExecution.tryTemporary(() -> {
            new File(getServerLevelPath(getServer())  + "/modmdo/resources/lang/").mkdirs();

            for (File f : EntrustParser.getNotNull(new File(getServerLevelPath(getServer())  + "/modmdo/resources/lang/").listFiles(), new File[0])) {
                resource.set(Language.ofs(f.getName()), f.getAbsolutePath());
            }
        });
        SharedVariables.consoleTextFormat = new ConsoleTextFormat(resource);
        SharedVariables.minecraftTextFormat = new MinecraftTextFormat(resource);
    }

    public boolean needEnsure() {
        return true;
    }
}