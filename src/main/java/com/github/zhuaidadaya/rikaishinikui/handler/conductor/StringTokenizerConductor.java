package com.github.zhuaidadaya.rikaishinikui.handler.conductor;

import com.github.zhuaidadaya.rikaishinikui.handler.conductor.iterator.*;
import org.jetbrains.annotations.*;

import java.util.*;

public record StringTokenizerConductor(StringTokenizer tokenizer) implements Iterable<String> {
    @NotNull
    @Override
    public Iterator<String> iterator() {
        return new StringTokenizerIterator(tokenizer);
    }

    public int size() {
        return tokenizer.countTokens();
    }
}
