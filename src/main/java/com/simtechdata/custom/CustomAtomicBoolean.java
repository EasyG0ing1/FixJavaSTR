package com.simtechdata.custom;

import java.util.concurrent.atomic.AtomicBoolean;

public class CustomAtomicBoolean {

    public CustomAtomicBoolean(boolean value) {
        flag.set(value);
    }

    private final AtomicBoolean flag = new AtomicBoolean();

    public boolean get() {
        return flag.get();
    }

    public void set(boolean value) {
        flag.set(value);
    }

    public void setTrue() {
        flag.set(true);
    }

    public void setFalse() {
        flag.set(false);
    }

    public boolean isFalse() {
        return !flag.get();
    }

    public boolean isTrue() {
        return flag.get();
    }
}
