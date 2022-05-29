package com.github.cao.awa.modmdo.listeners;

import com.github.cao.awa.modmdo.*;
import com.github.cao.awa.modmdo.commands.*;
import com.github.cao.awa.modmdo.network.forwarder.process.*;
import com.github.cao.awa.modmdo.storage.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;

import static com.github.cao.awa.modmdo.storage.SharedVariables.*;

public class ServerStartListener {
    public void listener() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SharedVariables.server = server;

            EntrustExecution.tryTemporary(() -> {
                commandRegister = new ModMdoCommandRegister(server);

                ModMdoStdInitializer.initForLevel(server);
            });
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (SharedVariables.isActive()) {
                tps.init(server, - 1);
                event.submitServerStarted(server);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            EntrustExecution.tryFor(modmdoConnections, ModMdoDataProcessor::disconnect);
            tps.stop();
        });
    }
}