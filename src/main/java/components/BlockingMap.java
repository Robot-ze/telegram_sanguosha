package components;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import sanguosha.manager.GameManager;

public class BlockingMap<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, MyCountDownLatch> latchMap = new ConcurrentHashMap<>();
    private LatchManager latchManager;

    public BlockingMap(LatchManager latchManager) {
        this.latchManager = latchManager;
    }

    // 获取键值，带超时
    public V get(K key, long timeout, TimeUnit unit) throws InterruptedException {
        V value = map.get(key);
        if (value != null) {
            return value;
        }

        MyCountDownLatch realLatch = latchMap.computeIfAbsent(key, k -> MyCountDownLatch.newInst(1, latchManager));
        value = map.get(key);
        if (value != null) {
            return value;
        }
        // 等待指定时间
        boolean completed = realLatch.await(timeout, unit);

        if (completed) {
            return map.get(key); // 返回最新的值
        } else {
            return null; // 超时返回 null
        }
    }

    // 设置键值
    public void put(K key, V value) {
        map.put(key, value);

        // 唤醒等待的线程
        MyCountDownLatch latch = latchMap.remove(key);
        if (latch != null) {
            latch.countDown();
        }
    }

    // 删除键值
    public void del(K key) {
        map.remove(key);

        // 如果有线程在等待这个 key，则唤醒它
        MyCountDownLatch latch = latchMap.remove(key);
        if (latch != null) {
            latch.countDown();
        }
    }

    // 如果键不存在，则设置键值
    public V putIfAbsent(K key, V value) {
        V existingValue = map.putIfAbsent(key, value);
        if (existingValue == null) {
            // 如果键不存在，则唤醒等待的线程
            MyCountDownLatch latch = latchMap.remove(key);
            if (latch != null) {
                latch.countDown();
            }
        }
        return existingValue;
    }

    /**
     * 是否一个key的阻塞，因为在其他地方已经取到值了
     * 
     * @param key
     */
    public void unlock(K key) {
        MyCountDownLatch latch = latchMap.get(key);
        if (latch != null) {
            while (latch.getCount() > 0) {
                latch.countDown();
            }
        }
    }
 
    public Set<K> getExistsKeys() {
        return latchMap.keySet();
    }

    // 清除所有键值对
    public void clear() {
        map.clear();
        latchMap.clear();
    }

    @Override
    public String toString() {
        return "BlockingMap{" +
                "map=" + map +
                ", latchMap=" + latchMap +
                '}';
    }

}
