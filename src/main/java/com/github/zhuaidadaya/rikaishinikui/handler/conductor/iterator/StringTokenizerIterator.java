package com.github.zhuaidadaya.rikaishinikui.handler.conductor.iterator;

import java.util.*;

public record StringTokenizerIterator(StringTokenizer tokenizer) implements Iterator<String> {
    @Override
    public boolean hasNext() {
        return tokenizer.hasMoreTokens();
    }

    @Override
    public String next() {
        return tokenizer.nextToken();
    }
}