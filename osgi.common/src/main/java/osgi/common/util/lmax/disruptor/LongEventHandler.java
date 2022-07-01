package osgi.common.util.lmax.disruptor;

import com.lmax.disruptor.EventHandler;

public class LongEventHandler implements EventHandler<LongEvent> {

    long t1 = 0;

    long t2 = 0;

    long t3 = 0;

    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
        long value = event.getValue();
        System.out.println(value);
        if (value == 0) {
            t1 = System.currentTimeMillis();
        }
        if (value < 1000000 - 3) {
            return;
        }
        if (value == 1000000 - 2) {
            t2 = System.currentTimeMillis();
        }
        if (value == 1000000 - 1) {
            t3 = System.currentTimeMillis();
        }
        if (t2 != 0 && t3 != 0) {
            System.out.println(value + ":" + Thread.currentThread().getName());
            long end = Math.max(t2, t3);
            System.out.println("t1=" + t1);
            System.out.println("t2=" + t2);
            System.out.println("t3=" + t3);
            System.out.println(end - t1);
        }
    }

}
