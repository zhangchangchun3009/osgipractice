package osgi.common.util.lmax.disruptor;

import java.nio.ByteBuffer;

import com.lmax.disruptor.RingBuffer;

/**
 * notice this: this class is just a plain java class, the core method 
 * is ringBuffer. publish or ringBuffer.publishEvent
 * @author zhangchangchun
 * @Date 2022年3月8日
 */
public class LongEventProducer {

    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * <p>当用一个简单队列来发布事件的时候会牵涉更多的细节，这是因为事件对象还需要预先创建。
     * <p>发布事件最少需要两步：获取下一个事件槽并发布事件（发布事件的时候要使用try/finnally保证事件一定会被发布）。
     * <p>如果我们使用RingBuffer.next()获取一个事件槽，那么一定要发布对应的事件。
     * <p>如果不能发布事件，那么就会引起Disruptor状态的混乱。
     * <p>尤其是在多个事件生产者的情况下会导致事件消费者失速，从而不得不重启应用才能会恢复。
     * @param bb 模拟IO事件
     */
    public void onData(ByteBuffer bb) {
        long sequence = ringBuffer.next();
        try {
            LongEvent event = ringBuffer.get(sequence);
            event.setValue(bb.getLong(0));
        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
