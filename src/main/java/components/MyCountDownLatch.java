package components;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import sanguosha.manager.GameManager;

public class MyCountDownLatch {
    private CountDownLatch latch;
    private LatchManager latchManager;

    private MyCountDownLatch(int count, LatchManager latchManager) {
        latch = new CountDownLatch(count);
        this.latchManager = latchManager;
        latchManager.getLatchs().add(this);

    }

    public static MyCountDownLatch newInst(int count, LatchManager latchManager) {
        return new MyCountDownLatch(count, latchManager);
    }

    public void countDown() {
        latch.countDown();
    }

    public long getCount() {
        return latch.getCount();
    }

    public boolean await(long time, TimeUnit unit) {
        boolean done = false;
        try {
            done = latch.await(time, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            latchManager.getLatchs().remove(this);

        }
        return done;
    }
}
