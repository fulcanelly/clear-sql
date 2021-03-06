package me.fulcanelly.clsql.container;

import java.util.concurrent.*;

import lombok.SneakyThrows;

public class BlockingVariable<T> {
    final BlockingQueue<T> value = new ArrayBlockingQueue<>(1);

    boolean done = false;
    T cache;

    public boolean haveValue() {
        return done;
    } 

    public void setValue(T val) {
        value.add(val);
        cache = val;
        done = true;
    }

    @SneakyThrows
	public
    T getValue() {
        if (done) {
            return cache;
        } else {
            return value.take();
        }
    }
}
