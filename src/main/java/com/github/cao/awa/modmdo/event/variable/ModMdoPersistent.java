package com.github.cao.awa.modmdo.event.variable;

import com.github.cao.awa.modmdo.annotations.*;
import com.github.cao.awa.modmdo.utils.file.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import org.json.*;

import java.io.*;

@Auto
public abstract class ModMdoPersistent<T> {
    public File file;
    public String name;
    public JSONObject meta;

    public JSONObject getMeta() {
        return meta;
    }

    public void setMeta(JSONObject meta) {
        this.meta = meta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void save() {
        EntrustExecution.tryTemporary(() -> {
            JSONObject json = new JSONObject();
            json.put("variable", toJSONObject());
            FileUtil.write(file, json.toString());
        });
    }

    public abstract JSONObject toJSONObject();

    public abstract T get();

    public abstract ModMdoPersistent<T> build(File file, JSONObject json);

    public abstract ModMdoPersistent<T> clone();

    public void handle(JSONObject json) {

    }
}
