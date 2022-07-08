package com.github.zhuaidadaya.rikaishinikui.handler.universal.action;

import java.util.function.*;

public class Do {
    public static void letForDown(int count, Consumer<Integer> action) {
        for (int i = count;i > 0; i--) {
            action.accept(i);
        }
    }

    public static void letForUp(int count, Consumer<Integer> action) {
        for (int i = 0;count > i; i++) {
            action.accept(i);
        }
    }
}
