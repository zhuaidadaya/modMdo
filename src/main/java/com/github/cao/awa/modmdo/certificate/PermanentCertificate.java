package com.github.cao.awa.modmdo.certificate;

import com.github.cao.awa.modmdo.server.login.*;
import org.json.*;

import java.util.*;

public class PermanentCertificate extends Certificate {
    public PermanentCertificate(String name, String identifier, UUID uuid) {
        super(name, new LoginRecorde(identifier, uuid, identifier.equals("") ? LoginRecordeType.UUID : LoginRecordeType.IDENTIFIER));
    }

    public PermanentCertificate(String name, LoginRecorde recorde) {
        super(name, recorde);
    }

    public static PermanentCertificate build(JSONObject json) {
        return new PermanentCertificate(json.getString("name"), LoginRecorde.build(json.getJSONObject("recorder")));
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("recorder", recorde.toJSONObject());
        json.put("type", "permanent");
        json.put("name", getName());
        return json;
    }
}
