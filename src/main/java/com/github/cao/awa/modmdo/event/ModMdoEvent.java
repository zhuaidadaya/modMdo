package com.github.cao.awa.modmdo.event;

import com.github.cao.awa.hyacinth.logging.*;
import com.github.cao.awa.modmdo.event.delay.*;
import com.github.cao.awa.modmdo.utils.times.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.function.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.function.annotaions.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.operational.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.runnable.*;
import it.unimi.dsi.fastutil.objects.*;

import java.util.function.*;

import static com.github.cao.awa.modmdo.storage.SharedVariables.*;

@AsyncDelay
public abstract class ModMdoEvent<T extends ModMdoEvent<?>> {
    private final Object2ObjectLinkedOpenHashMap<TaskOrder<T>, Thread> await = new Object2ObjectLinkedOpenHashMap<>();
    private final ObjectLinkedOpenHashSet<TaskOrder<T>> actions = new ObjectLinkedOpenHashSet<>();
    private final ObjectArrayList<T> delay = new ObjectArrayList<>();
    private final ObjectArrayList<Previously<T>> previously = new ObjectArrayList<>();
    private boolean submitted = false;

    public synchronized void register(Consumer<T> action) {
        actions.add(new TaskOrder<>(action));
    }

    public boolean isSubmitted() {
        return submitted;
    }

    @AsyncDelay
    public synchronized void immediately(T target) {
        submit(target);
        action();
    }

    @AsyncDelay
    public synchronized void action(boolean enforce) {
        for (T target : delay) {
            for (TaskOrder<T> event : actions) {
                if (enforce) {
                    EntrustExecution.trying(target, event::enforce);
                } else {
                    EntrustExecution.trying(target, event::call);
                }
            }
            for (TaskOrder<T> event : await.keySet()) {
                await.get(event).interrupt();
                if (enforce) {
                    EntrustExecution.trying(target, event::enforce);
                } else {
                    EntrustExecution.trying(target, event::call);
                }
                await.remove(event);
            }
            delay.remove(target);
        }

        submitted = false;
    }

    public synchronized void action() {
        action(false);
    }

    @AsyncDelay
    public synchronized void submit(T target) {
        if (previously.size() > 0) {
            target = fuse(previously.get(0), target);
            previously.remove(0);
        }
        delay.add(target);
        submitted = true;
        if (testing) {
            PrintUtil.messageToTracker(PrintUtil.tacker(Thread.currentThread().getStackTrace(), - 1, target.synopsis()));
        }
    }

    public abstract T fuse(Previously<T> previously, T delay);

    public abstract String synopsis();

    @AsyncDelay
    public void await(Temporary action, int orWait) {
        orWait(new TaskOrder<>(e -> action.apply(), true), orWait);
    }

    public void orWait(TaskOrder<T> order, final int wait) {
        await.put(order, EntrustParser.thread(() -> {
            synchronized (await) {
                EntrustExecution.tryTemporary(() -> {
                    OperationalInteger integer = new OperationalInteger(wait);
                    while (integer.get() > 0) {
                        TimeUtil.barricade(10);
                        integer.reduce(10);
                        if (await.get(order).isInterrupted()) {
                            return;
                        }
                    }
                    if (await.containsKey(order)) {
                        order.call(null);
                        await.remove(order);
                    }
                });
            }
        }));
        await.get(order).start();
    }

    public synchronized void skipDelay(T target) {
        if (delay.size() > 0) {
            return;
        }
        submit(target);
        action();
    }

    public int registered() {
        return actions.size();
    }

    @AsyncDelay
    public synchronized void immediately(T target, Temporary action) {
        previously(target, action);
        submit(target);
        action();
    }

    @AsyncDelay
    public synchronized void previously(T target, Temporary action) {
        previously.add(new Previously<>(target, action));
    }

    @SingleThread
    public void refrainAsync(T target, Temporary action) {
        previously(target, action);
        submit(target);
        action(true);
    }

    @SingleThread
    public void refrainAsync(T target) {
        submit(target);
        action(true);
    }

    public void adaptive(T target) {
        immediately(target);
    }

    public void auto(ModMdoEvent<?> target) {
        if (target.clazz().equals(clazz())) {
            adaptive((T)target);
        }
    }

    public abstract String abbreviate();

    public abstract String clazz();
}
