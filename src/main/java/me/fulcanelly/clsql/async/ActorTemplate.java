package me.fulcanelly.clsql.async;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import me.fulcanelly.clsql.stop.*;

public abstract class ActorTemplate<T> extends Thread implements Stopable {

    @Override
    public void stopIt() {
        queue.add(StopSignalOrData.getStopSignal());
    }

    final BlockingQueue<StopSignalOrData<T>> queue = new LinkedBlockingQueue<>();

    abstract public void consume(T data);

    public void addOne(T data) {
        queue.add(new StopSignalOrData<>(data));
    }

    public void carefulConsume(T data) {
        try {
            consume(data);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void run() {
        while (true) {
            var res = Stream.of(queue.take())
                .filter(it -> !it.isSignal())
                .map(it -> it.get())
                .peek(this::carefulConsume)
                .findAny();

            if (res.isEmpty()) {
                return;
            }
        }
    }
    
}
