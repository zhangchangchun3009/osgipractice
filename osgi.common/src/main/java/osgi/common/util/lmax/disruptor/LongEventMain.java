package osgi.common.util.lmax.disruptor;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

/**
 *  on my pc, when ringBufferSize is 1024, handle event costs 14770ms,
 *  when the size is 1024 * 16  or over this, it costs about 2000ms.
 * @author zhangchangchun
 * @Date 2022年3月9日
 */
public class LongEventMain {

    public static void main(String[] args) throws InterruptedException {

        LongEventFactory factory = new LongEventFactory();

        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "disruptor-thread-" + System.currentTimeMillis());
            }
        };

        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(factory, 1024 * 16, threadFactory, ProducerType.MULTI,
                waitStrategy);
        disruptor.handleEventsWith(new LongEventHandler());
        disruptor.start();
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        LongEventProducer producer1 = new LongEventProducer(ringBuffer);
        LongEventProducer producer2 = new LongEventProducer(ringBuffer);
        Thread t1 = new Thread(() -> {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            for (long l = 0; l < 1000000; l += 2) {
                byteBuffer.putLong(0, l);
                producer1.onData(byteBuffer);
            }
        });
        Thread t2 = new Thread(() -> {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            for (long l = 1; l < 1000000; l += 2) {
                byteBuffer.putLong(0, l);
                producer2.onData(byteBuffer);
            }
        });
        t1.start();
        t2.start();
        Thread.sleep(20000);
        disruptor.shutdown();
    }

}
