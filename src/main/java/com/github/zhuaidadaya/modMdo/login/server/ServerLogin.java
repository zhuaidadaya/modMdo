package com.github.zhuaidadaya.modMdo.login.server;

import com.github.zhuaidadaya.modMdo.login.token.ClientEncryptionToken;
import com.github.zhuaidadaya.modMdo.usr.User;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.github.zhuaidadaya.modMdo.storage.Variables.*;

public class ServerLogin {
    public void login(String data1, String data2, String data3, String data4, String data5, String data6) {
        int level = 1;
        if(data3.equals("ops"))
            level = 4;

        if(! data1.equals("")) {
            if(data4.equals(modMdoToken.getServerToken().checkToken(data3))) {
                loginUsers.put(data1, new User(data2, data1, level, new ClientEncryptionToken(data4, data5, data3, data6)).toJSONObject());

                LOGGER.info("login player: " + data1);
            }
        }
    }

    public void login(String data1, String data2, String data3, String data4, String data5) {
        int level = 1;
        if(data3.equals("ops"))
            level = 4;

        if(! data1.equals("")) {
            loginUsers.put(data1, new User(data2, data1, level, new ClientEncryptionToken("", data3, data4, data5)).toJSONObject());

            LOGGER.info("login player: " + data1);
        }
    }

    public void logout(ServerPlayerEntity player) {
        LOGGER.info("logout player: " + player.getUuid().toString());
        LOGGER.info("canceling player token for: " + player.getUuid().toString());
        try {
            loginUsers.removeUser(player);
        } catch (Exception e) {

        }
    }
}
