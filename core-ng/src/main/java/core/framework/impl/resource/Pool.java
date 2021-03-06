package core.framework.impl.resource;

import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * this is for internal use only,
 * <p>
 * the reason not using template/lambda pattern but classic design
 * is to keep original exception, and simplify context variable access (read or write var within method),
 * <p>
 * the downside is boilerplate code, so to keep it only for internal
 *
 * @author neo
 */
public class Pool<T extends AutoCloseable> {
    final BlockingDeque<PoolItem<T>> idleItems = new LinkedBlockingDeque<>();
    final String name;
    final AtomicInteger size = new AtomicInteger(0);
    private final Logger logger = LoggerFactory.getLogger(Pool.class);
    private final Supplier<T> factory;
    public Duration maxIdleTime = Duration.ofMinutes(30);
    private int minSize = 1;
    private int maxSize = 50;
    private long checkoutTimeoutInMs = Duration.ofSeconds(30).toMillis();

    public Pool(Supplier<T> factory, String name) {
        this.factory = factory;
        this.name = name;
    }

    public void size(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public void checkoutTimeout(Duration timeout) {
        checkoutTimeoutInMs = timeout.toMillis();
    }

    public PoolItem<T> borrowItem() {
        PoolItem<T> item = idleItems.poll();
        if (item != null) return item;

        if (size.get() < maxSize) {
            return createNewItem();
        } else {
            return waitNextAvailableItem();
        }
    }

    public void returnItem(PoolItem<T> item) {
        if (item.broken) {
            closeResource(item.resource);
        } else {
            item.returnTime = System.currentTimeMillis();
            idleItems.push(item);
        }
    }

    private PoolItem<T> waitNextAvailableItem() {
        var watch = new StopWatch();
        try {
            PoolItem<T> item = idleItems.poll(checkoutTimeoutInMs, TimeUnit.MILLISECONDS);
            if (item == null) throw new PoolException("timeout to wait for next available resource", "POOL_TIME_OUT");
            return item;
        } catch (InterruptedException e) {
            throw new Error("interrupted during waiting for next available resource", e);
        } finally {
            logger.debug("wait for next available resource, pool={}, elapsed={}", name, watch.elapsed());
        }
    }

    private PoolItem<T> createNewItem() {
        var watch = new StopWatch();
        size.incrementAndGet();
        try {
            return new PoolItem<>(factory.get());
        } catch (Throwable e) {
            size.getAndDecrement();
            throw e;
        } finally {
            logger.debug("create new resource, pool={}, elapsed={}", name, watch.elapsed());
        }
    }

    public void refresh() {
        logger.info("refresh resource pool, pool={}", name);
        recycleIdleItems();
        replenish();
    }

    int activeCount() {
        return totalCount() - idleItems.size();
    }

    int totalCount() {
        return size.get();
    }

    private void recycleIdleItems() {
        Iterator<PoolItem<T>> iterator = idleItems.descendingIterator();
        long maxIdleTimeInMs = maxIdleTime.toMillis();
        long now = System.currentTimeMillis();

        while (iterator.hasNext()) {
            PoolItem<T> item = iterator.next();
            if (now - item.returnTime >= maxIdleTimeInMs) {
                boolean removed = idleItems.remove(item);
                if (!removed) return;
                closeResource(item.resource);
            } else {
                return;
            }
        }
    }

    private void replenish() {
        while (size.get() < minSize) {
            returnItem(createNewItem());
        }
    }

    private void closeResource(T resource) {
        size.decrementAndGet();
        try {
            resource.close();
        } catch (Exception e) {
            logger.warn("failed to close resource, pool={}", name, e);
        }
    }

    public void close() {
        size.set(maxSize);   // make sure no more new resource will be created
        while (true) {
            PoolItem<T> item = idleItems.poll();
            if (item == null) return;
            closeResource(item.resource);
        }
    }
}
