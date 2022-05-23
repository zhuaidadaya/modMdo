package com.github.zhuaidadaya.modmdo.event.trigger;

import com.github.zhuaidadaya.modmdo.event.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.function.annotaions.*;
import org.json.*;

@SingleThread
public abstract class ModMdoEventTrigger<T extends ModMdoEvent<?>> {
    public abstract void build(T event, JSONObject metadata);

    public abstract void action();
}
